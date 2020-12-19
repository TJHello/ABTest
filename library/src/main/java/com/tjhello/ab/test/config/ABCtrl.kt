package com.tjhello.ab.test.config

import android.content.Context
import android.os.Build
import java.util.*

/**
 * 作者:EYEWIND@TJHello
 * 时间:2020/12/19  10:20
 */
class ABCtrl {

    companion object {
        private const val CTRL_MAP_COUNTRY = "country"
        private const val CTRL_MAP_BRAND = "brand"
        private const val CTRL_MAP_MODEL = "model"
    }

    val whiteMap : MutableMap<String,Array<String>> ?= mutableMapOf()
    val blackMap : MutableMap<String,Array<String>> ?= mutableMapOf()
    val appVer: IntArray? = intArrayOf(0,0)//应用版本控制，正数是白名单，负数是黑名单
    val androidVer: IntArray? = intArrayOf(0,0)//Android版本范围，正数是白名单，负数是黑名单

    fun check(context: Context):Boolean{
        if(!this.whiteMap.isNullOrEmpty()){
            for (key in this.whiteMap.keys) {
                val valueArray = whiteMap[key]
                if(valueArray!=null){
                    if(!ctrlCheckWhileMap(key,valueArray)){
                        return false
                    }
                }
            }
        }
        if(!this.blackMap.isNullOrEmpty()){
            for (key in this.blackMap.keys) {
                val valueArray = blackMap[key]
                if(valueArray!=null){
                    if(!ctrlCheckBlackMap(key,valueArray)){
                        return false
                    }
                }
            }
        }
        if(appVer!=null&&appVer.isNotEmpty()){
            val packInfo = context.packageManager.getPackageInfo(context.packageName,0)
            val verBegin = this.appVer[0]
            val verEnd = this.appVer[1]
            if(verBegin>=0&&verEnd>=0){
                if(packInfo.versionCode<verBegin||packInfo.versionCode>verEnd){
                    return false
                }
            }else{
                if(packInfo.versionCode>=-verBegin&&packInfo.versionCode<=-verEnd){
                    return false
                }
            }
        }
        if(androidVer!=null&&androidVer.isNotEmpty()){
            val verBegin = this.androidVer[0]
            val verEnd = this.androidVer[1]
            val nowVer = Build.VERSION.SDK_INT
            if(verBegin>=0&&verEnd>=0){
                if( nowVer<verBegin || nowVer>verEnd){
                    return false
                }
            }else{
                if( nowVer>=-verBegin && nowVer<=-verEnd){
                    return false
                }
            }

        }
        return true
    }

    private fun ctrlCheckWhileMap(type:String,array:Array<String>):Boolean{
        when(type){
            CTRL_MAP_COUNTRY ->{
                if(!array.contains(Locale.getDefault().country)){
                    return false
                }
            }
            CTRL_MAP_BRAND ->{
                if(!array.contains(Build.MANUFACTURER)){
                    return false
                }
            }
            CTRL_MAP_MODEL ->{
                if(!array.contains(Build.MODEL)){
                    return false
                }
            }
        }
        return true
    }

    private fun ctrlCheckBlackMap(type:String,array:Array<String>):Boolean{
        when(type){
            CTRL_MAP_COUNTRY ->{
                if(array.contains(Locale.getDefault().country)){
                    return false
                }
            }
            CTRL_MAP_BRAND ->{
                if(array.contains(Build.BRAND)){
                    return false
                }
            }
            CTRL_MAP_MODEL ->{
                if(array.contains(Build.MODEL)){
                    return false
                }
            }
        }
        return true
    }

}