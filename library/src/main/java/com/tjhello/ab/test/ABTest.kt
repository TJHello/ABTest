package com.tjhello.ab.test

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.umeng.analytics.MobclickAgent
import java.io.File


/**
 * 创建者：TJbaobao
 * 时间:2020/2/24 10:21
 * 使用:
 * 说明:
 **/
class ABTest(private val context: Context) {

    companion object{
        private const val KEY_VERSION_CODE = "ab_test_version_code"
        private const val KEY_AB_HISTORY = "ab_test_ab_history"
        private const val TAG = "ABTestLog"

        private var abConfigList : MutableList<ABConfig> ?= null
        private var abHistoryMap = mutableMapOf<String,Int>()
        private var firstVersionCode = -1
        private var nowVersionCode = -1

        @JvmStatic
        fun init(context: Context,abConfigList: MutableList<ABConfig>?){
            if(abConfigList==null) return
            this.abConfigList = abConfigList

            val tools = Tools(context)
            nowVersionCode = getNowVersionCode(context)
            firstVersionCode = tools.getSharedPreferencesValue(KEY_VERSION_CODE,-1)?:-1
            if(firstVersionCode==-1){
                firstVersionCode = nowVersionCode
                tools.setSharedPreferencesValue(KEY_VERSION_CODE, firstVersionCode)
            }

            val abHistoryJSON = tools.getSharedPreferencesValue(KEY_AB_HISTORY, "")?:""
            try {
                abHistoryMap = if(abHistoryJSON.isEmpty()){
                    mutableMapOf()
                }else{
                    Gson().fromJson(abHistoryJSON, object : TypeToken<MutableMap<String, Int>>() {}.type)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        private fun getNowVersionCode(context: Context):Int{
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionCode
        }
    }

    fun getStringValue(name:String,def:String?) : String?{
        return getValue(name)?:def
    }

    fun getIntValue(name:String,def:Int):Int{
        val value = getValue(name)
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
        val value = getValue(name)
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
        if(abConfigList.isNullOrEmpty()) return
        val map = createMap(mutableMapOf("data" to data))
        eventBase(context,eventId,map)
    }

    fun event(eventId:String,mutableMap: MutableMap<String,String>){
        val map = createMap(mutableMap)
        eventBase(context,eventId,map)
    }



    private fun getValue(name: String):String?{
        abConfigList?.let {list->
            val abConfig = list.find {
                return@find it.name == name
            }
            if(abConfig!=null){
                if(canABTest(abConfig)){
                    val random = if(abHistoryMap.containsKey(name)){
                        abHistoryMap[name]?:(Math.random()*2).toInt()
                    }else{
                        (Math.random()*2).toInt()
                    }
                    abHistoryMap[name] = random
                    val tools = Tools(context)
                    tools.setSharedPreferencesValue(KEY_AB_HISTORY,Gson().toJson(abHistoryMap))
                    return if(random==0){
                        abConfig.dataA
                    }else{
                        abConfig.dataB
                    }
                }
            }
        }
        return null
    }

    private fun canABTest(abConfig: ABConfig):Boolean{
        val isPre = firstVersionCode>=abConfig.firstVersionCode//接入ABTest之后的用户
        val isNow = firstVersionCode>=abConfig.nowVersionCode//当前测试新增用户
        return if(abConfig.isOnlyNew) isPre&&isNow else isPre
    }

    private fun eventBase(context: Context,eventId: String,map: MutableMap<String,String>){
        MobclickAgent.onEvent(context,eventId,map)
        log("[event]:$eventId=>\n"+Gson().toJson(map))
    }

    private fun createMap(mutableMap: MutableMap<String, String>):MutableMap<String,String>{
        val keySet = mutableMap.keys
        keySet.forEach {
            val value = mutableMap[it]
            if(value!=null){
                abConfigList?.forEach {abConfig->
                    if(canABTest(abConfig)){
                        mutableMap[it+abConfig.name] = value
                    }
                }
            }
        }
        return mutableMap

    }

    private fun log(msg:String){
        Log.i(TAG,msg)
    }

}