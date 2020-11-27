package com.tjhello.ab.test

import android.content.Context
import android.os.SystemClock
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 作者:天镜baobao
 * 时间:2020/7/21  11:11
 * 说明:允许使用，但请遵循Apache License 2.0
 * 使用：
 * Copyright 2020/7/21 天镜baobao
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
class GameTimeHelper(private val context: Context,private val listener:Listener) {

    companion object {
        private const val SHARED_KEY = "ab_test_game_time_config"

    }

    private val timerTask = TJTimerTask(TimerRunnable())
    private val tools = Tools(context)
    private var tackInfoNow : TackInfo ?= null

    fun onPause(){
        tackInfoNow?.let {
            refreshTime(it)
        }
        stopTimer()
    }

    fun onResume(){
        tackInfoNow?.let {
            it.bT = SystemClock.elapsedRealtime()
            startTimer()
        }
    }

    fun startGame(name:String){
        val tackInfo = findTackInfo(name)
        if(tackInfo==null){
            val info = TackInfo().apply {
                this.bT = SystemClock.elapsedRealtime()
                this.name = name
            }
            saveInfo(info)
            tackInfoNow = info
            ABTest.log("startGame:${info.uT}")
            startTimer()
        }else{
            if(!tackInfo.isCom){
                tackInfoNow = tackInfo
                ABTest.log("startGame:${tackInfo.uT}")
                startTimer()
            }
        }
    }

    fun stopGame(name: String,isComplete:Boolean){
        val tackInfo = findTackInfo(name)
        if(tackInfo!=null&&!tackInfo.isCom){
            refreshTime(tackInfo)
            ABTest.log("stopGame:${tackInfo.uT},$isComplete")
            if(isComplete){
                tackInfo.isCom = true
                saveInfo(tackInfo)
                listener.onEvent(name,(tackInfo.uT/1000).toInt())
            }
        }
        tackInfoNow = null
        stopTimer()
    }

    private fun refreshTime(tackInfo: TackInfo){
        tackInfo.uT += SystemClock.elapsedRealtime()-tackInfo.bT
        tackInfo.bT = SystemClock.elapsedRealtime()
        saveInfo(tackInfo)
    }

    private fun startTimer(){
        timerTask.start(0,15000)
    }

    private fun stopTimer(){
        timerTask.stop()
    }

    private fun findTackInfo(name:String):TackInfo?{
        val key = SHARED_KEY+"_"+name
        val infoJson : String? = tools.getSharedPreferencesValue(key,null)
        if(!infoJson.isNullOrEmpty()){
            return Gson().fromJson(infoJson,TackInfo::class.java)
        }
        return null
    }

    private fun saveInfo(info:TackInfo){
        tools.setSharedPreferencesValue(SHARED_KEY+"_"+info.name,Gson().toJson(info))
    }

    @Keep
    private inner class TackInfo{
        var bT= 0L
        var uT = 0L
        var isCom= false
        var name = ""
    }

    private inner class TimerRunnable : Runnable{
        override fun run() {
            tackInfoNow?.let {
                synchronized(it) {
                    refreshTime(it)
                }
            }
        }
    }

    interface Listener{
        fun onEvent(name:String,time:Int)
    }

}