package com.tjhello.ab.test

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.google.firebase.remoteconfig.ktx.get
import com.google.gson.Gson

/**
 * 作者:EYEWIND@TJHello
 * 时间:2020/12/17  19:19
 */
object RemoteConfig {

    var isDebug = false
    private lateinit var remoteConfig : FirebaseRemoteConfig

    @JvmStatic
    fun init(onComplete:(result:Boolean)->Unit){
        init(0,onComplete)
    }

    @JvmStatic
    fun init(defXml:Int,onComplete:(result:Boolean)->Unit){
        remoteConfig = FirebaseRemoteConfig.getInstance()
        val builder = FirebaseRemoteConfigSettings.Builder()
        builder.minimumFetchIntervalInSeconds = if(isDebug) 50*5 else 3600*6
        setDefaults(defXml){
            remoteConfig.setConfigSettingsAsync(builder.build()).addOnCompleteListener {
                remoteConfig.fetchAndActivate().addOnCompleteListener {
                    onComplete(it.isSuccessful)
                }
            }
        }
    }

    @JvmStatic
    fun getLong(key:String):Long{
        return remoteConfig.getLong(key)
    }

    @JvmStatic
    fun getBoolean(key:String):Boolean{
        return remoteConfig.getBoolean(key)
    }

    @JvmStatic
    fun getString(key:String):String{
        return remoteConfig.getString(key)
    }

    @JvmStatic
    fun getDouble(key:String):Double{
        return remoteConfig.getDouble(key)
    }

    @JvmStatic
    fun getBaseValue(key:String):FirebaseRemoteConfigValue{
        return remoteConfig.getValue(key)
    }

    @JvmStatic
    fun <T> getJsonObj(key:String,aClass:Class<T>):T?{
        return try {
            Gson().fromJson(getString(key),aClass)
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun isRemote(key:String):Boolean{
        return remoteConfig[key].source == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE
    }

    private fun setDefaults(defXml:Int,function:()->Unit){
        if(defXml>0){
            remoteConfig.setDefaultsAsync(defXml).addOnCompleteListener {
                function()
            }
        }else{
            function()
        }
    }

    fun isOk():Boolean{
        return Tools.containsClass("com.google.firebase.remoteconfig.FirebaseRemoteConfig")
                && ::remoteConfig.isInitialized
    }

}