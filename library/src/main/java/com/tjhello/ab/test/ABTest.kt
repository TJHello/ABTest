@file:Suppress("MemberVisibilityCanBePrivate")

package com.tjhello.ab.test

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tjhello.ab.test.config.ABConfig
import com.tjhello.ab.test.config.ABValue
import com.tjhello.ab.test.config.OLConfig
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 作者:EYEWIND@TJHello
 * 时间:2020/12/17  19:14
 */
@Suppress("unused")
class ABTest private constructor(private val context: Context) {

    private val tools = Tools(context)

    companion object {

        private const val KEY_FIRST_VERSION_CODE = "ab_test_version_code"
        private const val KEY_FIRST_VERSION_NAME = "ab_test_version_name"
        private const val KEY_FIRST_DATE = "ab_test_first_date"
        private const val KEY_AB_HISTORY_V2 = "ab_test_ab_history_v2"
        private const val KEY_UNIQUE_USER = "ab_test_unique_user"
        private const val KEY_DAY_RETAIN = "ab_test_day_retain"
        private const val KEY_DAY_EVENT = "ab_test_day_event"
        private const val KEY_UID = "ab_test_uid"
        private const val TAG = "ABTestLog"

        var isOpenLogcat = true
        var isDebug = false
        //Firebase模式下，直接采用fixed的值来作为AB
        var isFirebaseAbMode = false

        private const val REMOTE_KEY = "ABTestConfig"

        @JvmStatic
        private lateinit var abTest : ABTest

        private val olConfig = OLConfig()

        private var firstVersionCode = -1
        private var firstVersionName = ""
        private var firstDate = ""
        private var nowVersionCode = -1
        private var hasUmeng = checkUmengSDK()
        private var hasFirebase = checkFirebaseSDK()

        private var abHistoryMap = mutableMapOf<String, ABValue>()
        private var exceptionCountryList = mutableListOf<String>()


        @JvmStatic
        fun init(context: Context, isFirstStart: Boolean): ABTest {
            val abTest = getInstance(context)
            val tools = Tools(context)

            nowVersionCode = getNowVersionCode(context)
            val nowVersionName = getNowVersionName(context)
            firstVersionCode = tools.getSharedPreferencesValue(KEY_FIRST_VERSION_CODE, -1)?:-1
            firstVersionName = tools.getSharedPreferencesValue(KEY_FIRST_VERSION_NAME,"")?:""
            val nowDate =  getDate()
            firstDate = tools.getSharedPreferencesValue(KEY_FIRST_DATE,nowDate)?: nowDate

            if(firstVersionCode ==-1){
                firstVersionCode = if(isFirstStart){
                    nowVersionCode
                }else{
                    1
                }
                tools.setSharedPreferencesValue(KEY_FIRST_VERSION_CODE, firstVersionCode)
                tools.setSharedPreferencesValue(KEY_FIRST_DATE, firstDate)
            }

            if(firstVersionName==""){
                firstVersionName = if(isFirstStart){
                    nowVersionName
                }else{
                    "0.0.1"
                }
                tools.setSharedPreferencesValue(KEY_FIRST_VERSION_NAME, firstVersionName)
            }

            val abHistoryJSON = tools.getSharedPreferencesValue(KEY_AB_HISTORY_V2, "")?:""
            abHistoryMap = try {
                if(abHistoryJSON.isEmpty()){
                    mutableMapOf()
                }else{
                    Gson().fromJson(abHistoryJSON, object : TypeToken<MutableMap<String, ABValue>>() {}.type)
                }
            }catch (e: Exception){
                e.printStackTrace()
                mutableMapOf()
            }

            var uid = tools.getSharedPreferencesValue(KEY_UID, "")
            if(uid.isNullOrEmpty()){
               uid = UUID.randomUUID().toString()
                tools.setSharedPreferencesValue(KEY_UID, uid)
            }

            if(hasFirebase){
                //添加基础用户属性
                FirebaseHandler.setUserProperty(context, "firstVersion", "$firstVersionCode")
                FirebaseHandler.setUserId(context, uid)
//                FirebaseHandler.setUserProperty(context, "uuid", "$uid")
                val width = tools.getScreenWidth()
                val height = tools.getScreenHeight()
                FirebaseHandler.setUserProperty(context, "deviceScreen", "$width*$height")
                log("userProperty:firstVersion=$firstVersionCode,uuid=$uid,deviceScreen=$width*$height")
            }
            return abTest
        }

        @JvmStatic
        fun addTestByRemoteConfig(){
            initOLConfig()
        }

        @JvmStatic
        fun addTestByJsonConfig(json: String?){
            if(json.isNullOrEmpty()) return
            try {
                val config = Gson().fromJson<OLConfig>(json, OLConfig::class.java)
                initOLConfig(config)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun addTestByInfoConfig(config: OLConfig?){
            initOLConfig(config)
        }

        @JvmStatic
        fun getInstance(context: Context?): ABTest {
            if(!Companion::abTest.isInitialized&&context!=null){
                abTest = ABTest(context)
            }
            return abTest
        }

        @JvmStatic
        fun getInstance():ABTest?{
            if(Companion::abTest.isInitialized){
               return abTest
            }
            return null
        }

        @JvmStatic
        fun isNewUser(versionCode: Int):Boolean{
            return firstVersionCode >=versionCode
        }

        @JvmStatic
        fun getFirstVersion():Int{
            return firstVersionCode
        }

        @JvmStatic
        fun getFirstVersionName():String{
            return firstVersionName
        }

        @JvmStatic
        fun allABTestName():MutableList<String>{
            val list = mutableListOf<String>()
            olConfig.allABConfig().forEach {
                list.add(it.name)
            }
            return list
        }

        @JvmStatic
        fun exceptionCountry(vararg countryList: String){
            exceptionCountryList.addAll(countryList)
        }

        private fun initOLConfig(){
            if(RemoteConfig.isOk()){
                val tempOLConfig = RemoteConfig.getJsonObj(REMOTE_KEY, OLConfig::class.java)
                initOLConfig(tempOLConfig)
            }
        }


        private fun getDate(): String {
            val dt = Date()
            val sdf = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH) as SimpleDateFormat
            sdf.timeZone = TimeZone.getTimeZone("Asia/Hong_Kong")
            sdf.applyPattern("yyyy-MM-dd")
            return sdf.format(dt)
        }



        private fun initOLConfig(config: OLConfig?){
            if(config!=null){
                if(config.log!=0){
                    isOpenLogcat = config.log==1
                }
                olConfig.copy(config){
                    abTest.addTest(it)
                }
            }
        }

        //获取应用当前的版本号
        private fun getNowVersionCode(context: Context):Int{
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionCode
        }

        private fun getNowVersionName(context: Context):String{
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        }

        private fun checkUmengSDK():Boolean{
            return Tools.containsClass("com.umeng.analytics.MobclickAgent")
        }

        private fun checkFirebaseSDK():Boolean{
            return Tools.containsClass("com.google.firebase.analytics.FirebaseAnalytics")
        }

        private fun eventBase(context: Context, eventId: String, map: MutableMap<String, String>){
            if(exceptionCountryList.contains(Locale.getDefault().country)) {
                log("[event-exception]:$eventId=>\n" + Gson().toJson(map))
                return
            }
            if(!isDebug){
                if(hasUmeng){
                    UMengHandler.event(context, eventId, map)
                }
                if(hasFirebase){
                    FirebaseHandler.event(context, eventId, map)
                }
            }
            log("[event]:$eventId=>\n" + Gson().toJson(map))
        }

        private fun eventBaseInt(context: Context, eventId: String, map: MutableMap<String, Int>){
            if(exceptionCountryList.contains(Locale.getDefault().country)) {
                log("[event-exception]:$eventId=>\n" + Gson().toJson(map))
                return
            }
            if(!isDebug){
                if(hasUmeng){
                    UMengHandler.eventObject(context, eventId, map)
                }
                if(hasFirebase){
                    FirebaseHandler.eventNum(context, eventId, map)
                }
            }
            log("[event]:$eventId=>\n" + Gson().toJson(map))
        }

        private fun eventBaseFirebase(context: Context, eventId: String, map: MutableMap<String, Any>){
            if(exceptionCountryList.contains(Locale.getDefault().country)) {
                log("[event-exception]:$eventId=>\n" + Gson().toJson(map))
                return
            }
            if(!isDebug){
                if(hasFirebase){
                    FirebaseHandler.eventAny(context, eventId, map)
                }
            }
            log("[eventDeepData]:$eventId=>\n" + Gson().toJson(map))
        }

        @JvmStatic
        internal fun log(msg: String){
            if(isOpenLogcat){
                Log.i(TAG, msg)
            }
        }
    }

    private var mapDayRetain = mutableMapOf<String, Int>()
    private var uniqueUser = tools.getSharedPreferencesValue(KEY_UNIQUE_USER, "")

    fun addTest(config: ABConfig): ABTest {
        if(olConfig.findTest(config.name)!=null) return this

        if(canTest(config)){
            olConfig.addTest(config)
            val dayEventKey = KEY_DAY_EVENT+"_"+config.name
            var dayEvent = tools.getSharedPreferencesValue(dayEventKey, "")

            val value = getValue(config.name, null)
            if(value!=null&&value.position>=0){
                val plan = getPlan(value.position, config.parentName)
                if(uniqueUser==null||!uniqueUser!!.contains(config.name)){
                    uniqueUser+="${config.name},"
                    eventBase(context, config.name, mutableMapOf("base".getVerTag(config.ver) to "独立用户_$plan"))
                    tools.setSharedPreferencesValue(KEY_UNIQUE_USER, uniqueUser)
                }
                eventBase(context, config.name, mutableMapOf("base".getVerTag(config.ver) to "启动应用_$plan"))

                val nowDayKey = getDate()
                //新的一天
                if(dayEvent.isNullOrEmpty()||!dayEvent.contains(nowDayKey)){
                    //获取留存天数
                    if(mapDayRetain.isEmpty()){
                        val dayRetain = tools.getSharedPreferencesValue(KEY_DAY_RETAIN, "")
                        if(!dayRetain.isNullOrEmpty()){
                            mapDayRetain = Gson().fromJson(dayRetain, object : TypeToken<MutableMap<String, Int>>() {}.type)
                        }
                    }
                    val dayNum = if(mapDayRetain.containsKey(config.name)){
                        mapDayRetain[config.name]?:0
                    }else{
                        0
                    }
                    //活跃
                    eventBase(context, config.name, mutableMapOf("base".getVerTag(config.ver) to "活跃用户_$plan"))
                    //只计算7天内的留存
                    if(dayNum<=7){
                        eventBase(context, config.name, mutableMapOf("day_$plan".getVerTag(config.ver) to "${firstDate}_$dayNum"))
                        //保存留存天数+1
                        mapDayRetain[config.name] = dayNum+1
                        tools.setSharedPreferencesValue(KEY_DAY_RETAIN, Gson().toJson(mapDayRetain))
                        //保存曾经记录过的日期
                        dayEvent+= "$nowDayKey,"
                        tools.setSharedPreferencesValue(dayEventKey, dayEvent)
                    }
                }

                if(hasFirebase){
                    FirebaseHandler.setUserProperty(context, "ABTest", config.name.getVerTag(config.ver) + "_" + plan)
                }

                log("参与了【${config.name.getVerTag(config.ver)}】测试【$plan】方案")
            }
        }
        return this
    }

    private fun String.getVerTag(ver: Int):String{
        return if(ver>0){
            this+"_"+ver
        }else{
            this
        }
    }

    fun fixedValue(name: String, value: String, onlyNew: Boolean = false): ABTest {
        olConfig.fixedValue(name, value, onlyNew)
        return this
    }

    //region===========================获取值===========================
    fun getString(name: String, def: String):String{
        val value = getValue(name, def)
        if(value!=null){
            return value.value
        }
        return def
    }

    fun getInt(name: String, def: Int):Int{
        val value = getString(name, "$def")
        if(value.isNotEmpty()){
            try {
                return value.toInt()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        return def
    }

    fun getFloat(name: String, def: Float):Float{
        val value = getString(name, "$def")
        if(value.isNotEmpty()){
            try {
                return value.toFloat()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        return def
    }

    fun getLong(name: String, def: Long):Long{
        val value = getString(name, "$def")
        if(value.isNotEmpty()){
            try {
                return value.toLong()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        return def
    }

    fun getPlan(name:String,def: String):String{
        val value = getValue(name,null)
        if(value==null||value.position<0){
            return def
        }else{
            olConfig.allABConfig().forEach { abConfig ->
                if(abConfig.name==name){
                    return getPlan(value.position,abConfig.parentName)
                }
            }
            return def;
        }
    }

    fun <T>getJsonInfo(name: String, aClass: Class<T>):T?{
        val value = getString(name, "")
        if(value.isNotEmpty()){
            try {
                return Gson().fromJson(value, aClass)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        return null
    }
    //endregion

    //region===========================打点===========================

    fun event(eventId: String, data: String){
        val map = createMap(eventId, mutableMapOf("data" to data))
        eventBase(context, eventId, map)
    }

    fun event(eventId: String, data: MutableMap<String, String>){
        val map = createMap(eventId, data)
        eventBase(context, eventId, map)
    }

    fun event(eventId: String, data: Int){
        val map = createMapInt(eventId, mutableMapOf("data" to data))
        eventBaseInt(context, eventId, map)
    }

    fun eventInt(eventId: String, data: MutableMap<String, Int>){
        val map = createMapInt(eventId, data)
        eventBaseInt(context, eventId, map)
    }

    /**
     * Firebase深度数据打点(不参与下划线规则)
     */
    fun eventFirebaseDeepData(eventId: String, data: MutableMap<String, Any>){
        eventBaseFirebase(context, eventId, data)
    }

    //endregion

    private fun canTest(abConfig: ABConfig):Boolean{
        //父子测试判断
        abConfig.parentName?.let { parentTest->
            if(parentTest.isNotEmpty()){
                val value = getValue(parentTest, null)
                if(value==null || (!abConfig.parentValue.isNullOrEmpty() && abConfig.parentValue!=value.value)){
                    return false
                }
            }
        }
        //控制体判断
        abConfig.ctrl?.let {
            if(!it.check(context)){
                return false
            }
        }
        //版本号判断
        val isNow = firstVersionCode >=abConfig.abVer//当前测试新增用户
        return if(abConfig.onlyNew) isNow else true
    }

    fun canTest(name: String):Boolean{
        val config = olConfig.findTest(name)
        if(config!=null){
            val value = getValue(name, null)
            return canTest(config) && (value!=null&&value.position>=0)
        }
        return false
    }

    private fun getValue(name: String, def: String?): ABValue?{
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
                        val abValue = ABValue(random, abConfig.data[random])
                        abHistoryMap[name] = abValue
                        if(def==null){
                            log("[getValue]:$name,${abValue.position},${abValue.value}")
                        }
                        tools.setSharedPreferencesValue(KEY_AB_HISTORY_V2, Gson().toJson(abHistoryMap))
                        return abValue
                    }else{
                        //没有获取到AB测试的时候，第一个获取的非空值，将被设为固定值
                        if(def!=null){
                            val value = ABValue(def)
                            abHistoryMap[name] = value
                            log("[getValue-def]:$name,${value.position},${value.value}")
                            tools.setSharedPreferencesValue(KEY_AB_HISTORY_V2, Gson().toJson(abHistoryMap))
                            return value
                        }
                    }
                }else{
                    //返回历史值
                    return adHistory
                }
            }else{
                //返回固定值
                return ABValue(fixedValue)
            }
        }
        log("[getValue-null]")
        return null
    }

    /**
     * 基于AB方案创建一个新的eventMap
     */
    private fun createMap(eventId: String, mutableMap: MutableMap<String, String>):MutableMap<String, String>{
        synchronized(olConfig) {
            val keySet = mutableMap.keys.toHashSet()
            keySet.forEach{ parameter->//遍历该事件所有维度
                val value = mutableMap[parameter]
                if(value!=null){
                    if(isFirebaseAbMode){
                        olConfig.allFixedName().forEach { name->
                            val fixed = olConfig.getFixedValue(name)
                            if(fixed!=null){
                                mutableMap[parameter + "_" + name + "_" + fixed.value] = value
                            }
                        }
                    }else{
                        //遍历所有AB方案
                        olConfig.allABConfig().forEach { abConfig->
                            //如果没有指定监听的事件或者该事件是指定监听的事件
                            if(abConfig.listenEvent.isNullOrEmpty()
                                ||abConfig.listenEvent.contains(eventId)
                                ||eventId==abConfig.name){
                                //判断大环境是否参与AB测试
                                if(canTest(abConfig)){
                                    //获取AB的值
                                    val data = getValue(abConfig.name, null)
                                    if(data!=null&&data.position>=0){//如果position小于0，代表不参与AB
                                        val plan = getPlan(data.position, abConfig.parentName)
                                        if(abConfig.mergeEvent){
                                            val  baseMutableMap = mutableMapOf<String, String>()
                                            if(abConfig.mergeTag){
                                                baseMutableMap[parameter + "_" + abConfig.name.getVerTag(abConfig.ver) + "_AB"] = value+"_"+plan
                                            }else{
                                                baseMutableMap[parameter + "_" + abConfig.name.getVerTag(abConfig.ver) + "_" + plan] = value
                                            }
                                            eventBase(context, abConfig.name, baseMutableMap)
                                        }else{
                                            if(abConfig.mergeTag){
                                                mutableMap[parameter + "_" + abConfig.name.getVerTag(abConfig.ver) + "_AB"] = value+"_"+plan
                                            }else{
                                                mutableMap[parameter + "_" + abConfig.name.getVerTag(abConfig.ver) + "_" + plan] = value
                                            }
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

    private fun createMapInt(eventId: String, mutableMap: MutableMap<String, Int>):MutableMap<String, Int>{
        synchronized(olConfig) {
            val keySet = mutableMap.keys.toHashSet()
            keySet.forEach{ parameter->
                val value = mutableMap[parameter]
                if(value!=null){
                    if(isFirebaseAbMode){
                        olConfig.allFixedName().forEach { name->
                            val fixed = olConfig.getFixedValue(name)
                            if(fixed!=null){
                                mutableMap[parameter + "_" + name + "_" + fixed.value] = value
                            }
                        }
                    }else{
                        olConfig.allABConfig().forEach { abConfig->
                            if(abConfig.listenEvent.isNullOrEmpty()
                                    ||abConfig.listenEvent.contains(eventId)
                                    ||eventId==abConfig.name)
                                if(canTest(abConfig)){
                                    val data = getValue(abConfig.name, null)
                                    if(data!=null&&data.position>=0){
                                        val plan = getPlan(data.position, abConfig.parentName)
                                        if(abConfig.mergeEvent){
                                            val  baseMutableMap = mutableMapOf<String, Int>()
                                            baseMutableMap[parameter.getVerTag(abConfig.ver) + "_" + plan] = value
                                            eventBaseInt(context, abConfig.name, baseMutableMap)
                                        }else{
                                            mutableMap[parameter.getVerTag(abConfig.ver) + "_" + plan] = value
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

    private val planArray = arrayOf("A", "B", "C", "D", "E", "F", "G", "H")
    private fun getPlan(position: Int, parent: String?):String{
        var plan = if(position<planArray.size){
            planArray[position]
        }else{
            "$position"
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
            val abValue = ABValue(-1, fixedValue.value)
            abHistoryMap[name] = abValue
            tools.setSharedPreferencesValue(KEY_AB_HISTORY_V2, Gson().toJson(abHistoryMap))

            return fixedValue.value
        }
    }

    private fun getAdHistory(name: String): ABValue?{
        return if(abHistoryMap.containsKey(name)){
            abHistoryMap[name]
        }else{
            null
        }
    }

}