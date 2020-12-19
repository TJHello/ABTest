@file:Suppress("MemberVisibilityCanBePrivate")

package com.tjhello.ab.test

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tjhello.ab.test.config.ABConfig
import com.tjhello.ab.test.config.ABValue
import com.tjhello.ab.test.config.OLConfig
import java.util.*

/**
 * 作者:EYEWIND@TJHello
 * 时间:2020/12/17  19:14
 */
@Suppress("unused")
class ABTest(private val context: Context) {

    private val tools = Tools(context)

    companion object {

        private const val KEY_VERSION_CODE = "ab_test_version_code"
        private const val KEY_AB_HISTORY_V2 = "ab_test_ab_history_v2"
        private const val KEY_UNIQUE_USER = "ab_test_unique_user"
        private const val KEY_DAY_RETAIN = "ab_test_day_retain"
        private const val KEY_DAY_EVENT = "ab_test_day_event"
        private const val TAG = "ABTestLog"

        var isOpenLogcat = true
        var isDebug = false
        //Firebase模式下，直接采用fixed的值来作为AB
        var isFirebaseAbMode = false

        private const val REMOTE_KEY = "ABTestConfig"

        private lateinit var abTest : ABTest

        private val olConfig = OLConfig()

        private var firstVersionCode = -1
        private var nowVersionCode = -1
        private var hasUmeng = checkUmengSDK()
        private var hasFirebase = checkFirebaseSDK()

        private var abHistoryMap = mutableMapOf<String, ABValue>()


        @JvmStatic
        fun init(context: Context,isFirstStart:Boolean): ABTest {
            val abTest = getInstance(context)
            val tools = Tools(context)
            initOLConfig()
            nowVersionCode = getNowVersionCode(context)
            firstVersionCode = tools.getSharedPreferencesValue(KEY_VERSION_CODE,-1)?:-1
            if(firstVersionCode ==-1){
                firstVersionCode = if(isFirstStart){
                    nowVersionCode
                }else{
                    1
                }
                tools.setSharedPreferencesValue(KEY_VERSION_CODE, firstVersionCode)
            }
            val abHistoryJSON = tools.getSharedPreferencesValue(KEY_AB_HISTORY_V2, "")?:""
            abHistoryMap = try {
                if(abHistoryJSON.isEmpty()){
                    mutableMapOf()
                }else{
                    Gson().fromJson(abHistoryJSON, object : TypeToken<MutableMap<String, ABValue>>() {}.type)
                }
            }catch (e:Exception){
                e.printStackTrace()
                mutableMapOf()
            }
            return abTest
        }

        @JvmStatic
        fun getInstance(context: Context): ABTest {
            if(!Companion::abTest.isInitialized){
                abTest = ABTest(context)
            }
            return abTest
        }

        @JvmStatic
        fun isNewUser(versionCode:Int):Boolean{
            return firstVersionCode >=versionCode
        }

        @JvmStatic
        fun getFirstVersion():Int{
            return firstVersionCode
        }

        @JvmStatic
        fun allABTestName():MutableList<String>{
            val list = mutableListOf<String>()
            olConfig.allABConfig().forEach {
                list.add(it.name)
            }
            return list
        }

        private fun initOLConfig(){
            if(RemoteConfig.isOk()){
                val tempOLConfig = RemoteConfig.getJsonObj(REMOTE_KEY, OLConfig::class.java)
                if(tempOLConfig!=null){
                    olConfig.copy(tempOLConfig){
                        abTest.addTest(it)
                    }
                }
            }
        }

        //获取应用当前的版本号
        private fun getNowVersionCode(context: Context):Int{
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionCode
        }

        private fun checkUmengSDK():Boolean{
            return Tools.containsClass("com.umeng.analytics.MobclickAgent")
        }

        private fun checkFirebaseSDK():Boolean{
            return Tools.containsClass("com.google.firebase.analytics.FirebaseAnalytics")
        }

        private fun eventBase(context: Context,eventId: String,map: MutableMap<String,String>){
            if(!isDebug){
                if(hasUmeng){
                    UMengHandler.event(context,eventId,map)
                }
                if(hasFirebase){
                    FirebaseHandler.event(context,eventId,map)
                }
            }
            log("[event]:$eventId=>\n"+Gson().toJson(map))
        }

        @JvmStatic
        internal fun log(msg:String){
            if(isOpenLogcat){
                Log.i(TAG,msg)
            }
        }
    }

    fun addTest(config: ABConfig): ABTest {
        if(olConfig.findTest(config.name)!=null) return this
        olConfig.addTest(config)
        var uniqueUser = tools.getSharedPreferencesValue(KEY_UNIQUE_USER,"")
        val dayRetain = tools.getSharedPreferencesValue(KEY_DAY_RETAIN,"")
        var dayEvent = tools.getSharedPreferencesValue(KEY_DAY_EVENT,"")

        val value = getValue(config.name)
        if(value!=null&&value.position>=0){
            val plan = getPlan(value.position,config.ver,config.parentName)
            if(uniqueUser==null||!uniqueUser.contains(config.name)){
                uniqueUser+="${config.name},"
                eventBase(context,config.name,mutableMapOf("base" to "独立用户_$plan"))
                tools.setSharedPreferencesValue(KEY_UNIQUE_USER,uniqueUser)
            }
            eventBase(context,config.name, mutableMapOf("base" to "启动应用_$plan"))


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
                eventBase(context,config.name, mutableMapOf("base" to "活跃用户_$plan"))
                eventBase(context,config.name, mutableMapOf("day_$plan" to "$dayNum"))
            }
            dayEvent+= "$nowDayKey,"
            tools.setSharedPreferencesValue(KEY_DAY_EVENT,dayEvent)

            mapDayRetain[config.name] = dayNum+1
            tools.setSharedPreferencesValue(KEY_DAY_RETAIN,Gson().toJson(mapDayRetain))

            if(hasFirebase){
                FirebaseHandler.setUserProperty(context,"ABTest",plan)
            }

        }
        return this
    }

    fun fixedValue(name: String,value:String,onlyNew:Boolean=false): ABTest {
        olConfig.fixedValue(name,value,onlyNew)
        return this
    }

    fun getString(name:String,def:String?):String?{
        val value = getValue(name)
        if(value!=null){
            return value.value
        }
        return def
    }

    fun getInt(name:String,def:Int):Int{
        val value = getString(name,"$def")
        if(!value.isNullOrEmpty()){
            try {
                return value.toInt()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        return def
    }

    fun getFloat(name:String,def:Float):Float{
        val value = getString(name,"$def")
        if(!value.isNullOrEmpty()){
            try {
                return value.toFloat()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        return def
    }

    fun getLong(name:String,def:Long):Long{
        val value = getString(name,"$def")
        if(!value.isNullOrEmpty()){
            try {
                return value.toLong()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        return def
    }

    fun <T>getJsonInfo(name:String,aClass:Class<T>):T?{
        val value = getString(name,null)
        if(!value.isNullOrEmpty()){
            try {
                return Gson().fromJson(value,aClass)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        return null
    }

    fun event(eventId: String,data:String){
        val map = createMap(eventId,mutableMapOf("data" to data))
        eventBase(context,eventId,map)
    }

    fun event(eventId: String,data:MutableMap<String,String>){
        val map = createMap(eventId,data)
        eventBase(context,eventId,map)
    }

    private fun canTest(abConfig: ABConfig):Boolean{
        abConfig.parentName?.let { parentTest->
            if(parentTest.isNotEmpty()){
                val value = getValue(parentTest)
                if(value==null || (!abConfig.parentValue.isNullOrEmpty() && abConfig.parentValue!=value.value)){
                    return false
                }
            }
        }
        abConfig.ctrl?.let {
            if(!it.check(context)){
                return false
            }
        }
        val isNow = firstVersionCode >=abConfig.abVer//当前测试新增用户
        return if(abConfig.onlyNew) isNow else true
    }

    private fun getValue(name:String): ABValue?{
        synchronized(olConfig){
            val fixedValue = getFixedValue(name)
            if(fixedValue==null){
                //没有固定值
                val adHistory = getAdHistory(name)
                if(adHistory==null){
                    //没有历史值
                    val abConfig = olConfig.findTest(name)
                    if(abConfig!=null){
                        val testSize = abConfig.data.size
                        val random = (Math.random()*testSize).toInt()
                        val abValue = ABValue(random,abConfig.data[random])
                        abHistoryMap[name] = abValue
                        tools.setSharedPreferencesValue(KEY_AB_HISTORY_V2,Gson().toJson(abHistoryMap))
                        return abValue
                    }
                }else{
                    return adHistory
                }
            }else{
                return ABValue(fixedValue)
            }
        }
        return null
    }

    private fun createMap(eventId: String,mutableMap: MutableMap<String,String>):MutableMap<String,String>{
        synchronized(olConfig) {
            val keySet = mutableMap.keys.toHashSet()
            keySet.forEach{parameter->
                val value = mutableMap[parameter]
                if(value!=null){
                    if(isFirebaseAbMode){
                        olConfig.allFixedName().forEach { name->
                            val fixed = olConfig.getFixedValue(name)
                            if(fixed!=null){
                                mutableMap[parameter+"_"+name+"_"+fixed.value] = value
                            }
                        }
                    }else{
                        olConfig.allABConfig().forEach { abConfig->
                            if(abConfig.listenEvent.isNullOrEmpty()
                                ||abConfig.listenEvent.contains(eventId)
                                ||eventId==abConfig.name)
                            if(canTest(abConfig)){
                                val data = getValue(abConfig.name)
                                if(data!=null&&data.position>=0){
                                    val plan = getPlan(data.position,abConfig.ver,abConfig.parentName)
                                    if(abConfig.mergeEvent){
                                        val  baseMutableMap = mutableMapOf<String,String>()
                                        if(abConfig.mergeTag){
                                            baseMutableMap[parameter+"_AB"] = value+"_"+plan
                                        }else{
                                            baseMutableMap[parameter+"_"+plan] = value
                                        }
                                        eventBase(context,abConfig.name,baseMutableMap)
                                    }else{
                                        if(abConfig.mergeTag){
                                            mutableMap[parameter+"_AB"] = value+"_"+plan
                                        }else{
                                            mutableMap[parameter+"_"+plan] = value
                                        }
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

    private val planArray = arrayOf("A","B","C","D")
    private fun getPlan(position: Int,ver:Int,parent:String?):String{
        var plan = if(position<4){
            planArray[position]
        }else{
            "$position"
        }
        if(ver>0){
            plan+= "_$ver"
        }
        if(!parent.isNullOrEmpty()){
            plan = parent+"_"+plan
        }
        return plan
    }

    private fun getFixedValue(name: String):String?{
        val fixedValue = olConfig.getFixedValue(name)
        if(fixedValue==null){
            //没有固定值
            return null
        }else{
            //存在固定值
            if(fixedValue.onlyNew){
                //仅限新用户&没有保存的历史
                val abHistory = getAdHistory(name)
                if(abHistory!=null){
                    return abHistory.value
                }
            }
            val abValue = ABValue(-1,fixedValue.value)
            abHistoryMap[name] = abValue
            tools.setSharedPreferencesValue(KEY_AB_HISTORY_V2,Gson().toJson(abHistoryMap))

            return fixedValue.value
        }
    }

    private fun getAdHistory(name:String): ABValue?{
        return if(abHistoryMap.containsKey(name)){
            abHistoryMap[name]
        }else{
            null
        }
    }

}