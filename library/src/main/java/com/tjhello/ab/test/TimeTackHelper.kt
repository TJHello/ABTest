package com.tjhello.ab.test

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 作者:天镜baobao
 * 时间:2020/6/12  14:16
 * 说明:允许使用，但请遵循Apache License 2.0
 * 使用：
 * Copyright 2020/6/12 天镜baobao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class TimeTackHelper(private val context: Context) {

    companion object {
        private const val TIME_PERIOD = 60*1000L
        private const val SHARED_KEY = "ab_test_time_tack_config"
    }

    var listener : Listener?= null

    private val timerTask = TJTimerTask(TimerRunnable())

    private val timeTackInfoList = mutableListOf<TimeTackInfo>()

    fun init(){
        val tools = Tools(context)
        val tackListJson: String ?= tools.getSharedPreferencesValue(SHARED_KEY,null)
        if(!tackListJson.isNullOrEmpty()){
            val tempList : MutableList<TimeTackInfo>?=Gson().fromJson(tackListJson,object : TypeToken<MutableList<TimeTackInfo>>(){}.type)
            if(!tempList.isNullOrEmpty()){
                timeTackInfoList.addAll(tempList)
            }
        }
        var hasTackOnce = false
        var hasTackDay = false
        for(i in timeTackInfoList.size-1 downTo 0){
            val info = timeTackInfoList[i]
            if(info.canUpload()){
                //可以提交并且删除
                if(info.useTime>0){
                    if(info.type==TimeTackInfo.TYPE_ONCE){
                        listener?.onEventOnce(info.useTime)
                    }else{
                        listener?.onEventDay(info.useTime)
                    }
                }
                timeTackInfoList.removeAt(i)
            }else{
                if(info.type==TimeTackInfo.TYPE_ONCE){
                    hasTackOnce = true
                }else{
                    hasTackDay = true
                }
            }
        }
        if(!hasTackOnce){
            val info = TimeTackInfo(TimeTackInfo.TYPE_ONCE).create()
            timeTackInfoList.add(info)
        }
        if(!hasTackDay){
            val info = TimeTackInfo(TimeTackInfo.TYPE_DAY).create()
            timeTackInfoList.add(info)
        }
        tools.setSharedPreferencesValue(SHARED_KEY,Gson().toJson(timeTackInfoList))
    }

    fun onPause(){
        ABTestOld.log("onPause")
        timeRefresh()
        timerTask.stop()

    }

    fun onResume(){
        ABTestOld.log("onResume")
        synchronized(timeTackInfoList){
            val size = timeTackInfoList.size
            for(i in size-1 downTo 0) {
                val info = timeTackInfoList[i]
                info.onResume()
            }
        }
        timerTask.start(TIME_PERIOD,TIME_PERIOD)
    }

    private fun timeRefresh(){
        synchronized(timeTackInfoList){
            val tools = Tools(context)
            val size = timeTackInfoList.size
            for(i in size-1 downTo 0) {
                val info = timeTackInfoList[i]
                if(info.refresh()){
                    //可以提交并且删除
                    if(info.type==TimeTackInfo.TYPE_ONCE){
                        listener?.onEventOnce(info.useTime)
                    }else{
                        listener?.onEventDay(info.useTime)
                    }
                    timeTackInfoList.removeAt(i)
                    val newInfo = TimeTackInfo(info.type).create()
                    timeTackInfoList.add(newInfo)
                }
            }
            tools.setSharedPreferencesValue(SHARED_KEY,Gson().toJson(timeTackInfoList))
        }
    }

    private inner class TimerRunnable : Runnable{
        override fun run() {
            timeRefresh()
        }
    }
    interface Listener {
        fun onEventOnce(time:Long)

        fun onEventDay(time:Long)
    }
}