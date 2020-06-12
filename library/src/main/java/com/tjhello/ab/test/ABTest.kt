package com.tjhello.ab.test

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.math.max


/**
 * 创建者：TJbaobao
 * 时间:2020/2/24 10:21
 * 使用:
 * 说明:
 * Copyright 2020 TJHello
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
class ABTest(private val context: Context) {

    companion object{
        private const val KEY_VERSION_CODE = "ab_test_version_code"
        private const val KEY_AB_HISTORY = "ab_test_ab_history"
        private const val KEY_UNIQUE_USER = "ab_test_unique_user"
        private const val KEY_DAY_RETAIN = "ab_test_day_retain"
        private const val KEY_DAY_EVENT = "ab_test_day_event"
        private const val TAG = "ABTestLog"
        var isDebug = false
        private var fixedValue = mutableMapOf<String,String>()

        private var abConfigList : MutableList<ABConfig> = mutableListOf()
        private var abHistoryMap = mutableMapOf<String,Int>()
        private var firstVersionCode = -1
        private var nowVersionCode = -1
        private var hasUmeng = checkUmengSDK()
        private var hasFirebase = checkFirebaseSDK()

        private lateinit var timeTackHelper: TimeTackHelper

        @JvmStatic
        fun init(context: Context,isNew:Boolean):ABTest{
            val tools = Tools(context)
            nowVersionCode = getNowVersionCode(context)
            firstVersionCode = tools.getSharedPreferencesValue(KEY_VERSION_CODE,-1)?:-1
            if(firstVersionCode==-1){
                firstVersionCode = if(isNew){
                    nowVersionCode
                }else{
                    max(1,nowVersionCode-1)
                }
                tools.setSharedPreferencesValue(KEY_VERSION_CODE, firstVersionCode)
            }
            val abHistoryJSON = tools.getSharedPreferencesValue(KEY_AB_HISTORY, "")?:""
            abHistoryMap = try {
                 if(abHistoryJSON.isEmpty()){
                    mutableMapOf()
                }else{
                    Gson().fromJson(abHistoryJSON, object : TypeToken<MutableMap<String, Int>>() {}.type)
                }
            }catch (e:Exception){
                e.printStackTrace()
                mutableMapOf()
            }
            return ABTest(context)
        }

        @JvmStatic
        fun isNewUser(versionCode:Int):Boolean{
            return firstVersionCode>=versionCode
        }

        @JvmStatic
        fun onPause(){
            timeTackHelper.onPause()
        }

        @JvmStatic
        fun onResume(){
            timeTackHelper.onResume()
        }

        @JvmStatic
        fun onExit(context: Context){
            if(hasUmeng){
                timeTackHelper.onPause()
                UMengHandler.onExit(context)
            }
        }

        private fun getNowVersionCode(context: Context):Int{
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionCode
        }

        private fun canABTest(abConfig: ABConfig):Boolean{
            val isPre = firstVersionCode>=abConfig.firstVersionCode//接入ABTest之后的用户
            val isNow = firstVersionCode>=abConfig.nowVersionCode//当前测试新增用户
            return if(abConfig.isOnlyNew) isPre&&isNow else isPre
        }

        private fun eventBase(context: Context,eventId: String,map: MutableMap<String,String>){
            if(hasUmeng){
                UMengHandler.event(context,eventId,map)
            }
            if(hasFirebase){
                FirebaseHandler.event(context,eventId,map)
            }
            log("[event]:$eventId=>\n"+Gson().toJson(map))
        }

        @JvmStatic
        internal fun log(msg:String){
            if(isDebug){
                Log.i(TAG,msg)
            }
        }

        private fun getValue(context : Context,name: String):String?{
            synchronized(abConfigList){
                val abConfig = abConfigList.find {
                    return@find it.name == name
                }
                if(abConfig!=null){
                    if(!fixedValue.containsKey(name)){
                        if(canABTest(abConfig)){
                            val testLength = abConfig.dataArray.size
                            var random = if(abHistoryMap.containsKey(name)){
                                abHistoryMap[name]?:(Math.random()*testLength).toInt()
                            }else{
                                (Math.random()*testLength).toInt()
                            }
                            if(random>=testLength){
                                random = (Math.random()*testLength).toInt()
                            }
                            abHistoryMap[name] = random
                            val tools = Tools(context)
                            tools.setSharedPreferencesValue(KEY_AB_HISTORY,Gson().toJson(abHistoryMap))
                            return abConfig.dataArray[random]
                        }
                    }else{
                        return fixedValue[name]
                    }
                }else{
                    if(fixedValue.containsKey(name)){
                        return fixedValue[name]
                    }
                }
            }
            return null
        }

        private fun checkUmengSDK():Boolean{
            return containsClass("com.umeng.analytics.MobclickAgent")
        }

        private fun checkFirebaseSDK():Boolean{
            return containsClass("com.google.firebase.analytics.FirebaseAnalytics")
        }

        private fun containsClass(name:String):Boolean{
            return try {
                Class.forName(name)
                true
            }catch (e:ClassNotFoundException){
                log("Not imported:$name")
                false
            }
        }

        private class TimeTackListener(private val context: Context) : TimeTackHelper.Listener{
            override fun onEventOnce(time: Long) {
                synchronized(abConfigList){
                    for (config in abConfigList) {
                        val value = getValue(context,config.name)
                        if(value!=null){
                            if(canABTest(config)){
                                eventBase(context,config.name, mutableMapOf("time_once_$value" to "${time/1000}"))
                            }
                        }
                    }
                }
            }

            override fun onEventDay(time: Long) {
                synchronized(abConfigList){
                    for (config in abConfigList) {
                        val value = getValue(context,config.name)
                        if(value!=null){
                            if(canABTest(config)){
                                eventBase(context,config.name, mutableMapOf("time_day_$value" to "${time/1000/60}"))
                            }
                        }
                    }
                }
            }
        }

        private fun createMap(context: Context,eventId: String,mutableMap: MutableMap<String, String>):MutableMap<String,String>{
            synchronized(abConfigList) {
                val keySet = mutableMap.keys.toHashSet()
                keySet.forEach {
                    val value = mutableMap[it]
                    if(value!=null){
                        abConfigList.forEach {abConfig->
                            if(abConfig.listenEventArray.isNullOrEmpty()||abConfig.listenEventArray.contains(eventId)){
                                if(canABTest(abConfig)){
                                    val data = getValue(context,abConfig.name)
                                    if(data!=null){
                                        mutableMap[it+"_"+abConfig.name+"_"+data] = value
                                        mutableMap[it+"_"+abConfig.name+"_"+"all"] = value
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return mutableMap

        }
    }

    fun addTest(context: Context,config:ABConfig):ABTest{
        abConfigList.add(config)
        val tools = Tools(context)
        var uniqueUser = tools.getSharedPreferencesValue(KEY_UNIQUE_USER,"")
        val dayRetain = tools.getSharedPreferencesValue(KEY_DAY_RETAIN,"")
        var dayEvent = tools.getSharedPreferencesValue(KEY_DAY_EVENT,"")
        val value = getValue(context,config.name)
        if(value!=null){
            if(canABTest(config)){
                if(uniqueUser==null||!uniqueUser.contains(config.name)){
                    uniqueUser+="${config.name},"
                    eventBase(context,config.name, mutableMapOf("base_$value" to "user"))
                    tools.setSharedPreferencesValue(KEY_UNIQUE_USER,uniqueUser)
                }
                eventBase(context,config.name, mutableMapOf("base_$value" to "startApp"))
                val mapDayRetain = if(dayRetain.isNullOrEmpty()) {
                    mutableMapOf<String,Int>()
                } else {
                    Gson().fromJson(dayRetain,object : TypeToken<MutableMap<String,Int>>(){}.type)
                }
                val dayNum = if(mapDayRetain.containsKey(config.name)){
                    mapDayRetain[config.name]?:0
                }else{
                    0
                }
                val nowDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                val nowDayKey = String.format(Locale.getDefault(),"%s:%02d",config.name,nowDay)
                if(dayEvent.isNullOrEmpty()||!dayEvent.contains(nowDayKey)){

                    eventBase(context,config.name, mutableMapOf("day_$value" to "$dayNum"))

                    dayEvent+= "$nowDayKey,"
                    tools.setSharedPreferencesValue(KEY_DAY_EVENT,dayEvent)

                    mapDayRetain[config.name] = dayNum+1
                    tools.setSharedPreferencesValue(KEY_DAY_RETAIN,Gson().toJson(mapDayRetain))
                }
            }
        }
        return this
    }

    fun fixedValue(name:String,value:String):ABTest{
        fixedValue[name] = value
        return this
    }

    fun startTimeTack():ABTest{
        timeTackHelper = TimeTackHelper(context)
        timeTackHelper.listener = TimeTackListener(context)
        timeTackHelper.init()
        return this
    }

    fun getStringValue(name:String,def:String?) : String?{
        return getValue(context,name)?:def
    }

    fun getIntValue(name:String,def:Int):Int{
        val value = getValue(context,name)
        if(value.isNullOrEmpty()){
            return def
        }else{
            try {
                return value.toInt()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        return def
    }

    fun getFloatValue(name:String,def:Float):Float{
        val value = getValue(context,name)
        if(value.isNullOrEmpty()){
            return def
        }else{
            try {
                return value.toFloat()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        return def
    }

    fun event(eventId:String,data:String){
        val map = createMap(context,eventId,mutableMapOf("data" to data))
        eventBase(context,eventId,map)
    }

    fun event(eventId:String,mutableMap: MutableMap<String,String>){
        val map = createMap(context,eventId,mutableMap)
        eventBase(context,eventId,map)
    }

}