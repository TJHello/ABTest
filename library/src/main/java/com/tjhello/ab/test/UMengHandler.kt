package com.tjhello.ab.test

import android.content.Context
import com.umeng.analytics.MobclickAgent
import java.util.concurrent.Executors


/**
 * 作者:EYEWIND@TJHello
 * 时间:2019/1/22  18:18
 * 说明:
 * 使用：
 */
object UMengHandler {

    @JvmStatic
    fun event(context: Context,eventId:String,map:MutableMap<String,String>){
        MobclickAgent.onEvent(context,eventId,map)
    }


    @JvmStatic
    fun eventObject(context: Context,eventId:String,map:MutableMap<String,out Any>){
        MobclickAgent.onEventObject(context,eventId,map)
    }

    fun onExit(context: Context){
        MobclickAgent.onKillProcess(context)
    }

}