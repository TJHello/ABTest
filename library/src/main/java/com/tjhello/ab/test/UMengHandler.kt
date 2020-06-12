package com.tjhello.ab.test

import android.content.Context
import com.umeng.analytics.MobclickAgent


/**
 * 作者:TJbaobao
 * 时间:2019/1/22  18:18
 * 说明:
 * 使用：
 */
object UMengHandler {

    @JvmStatic
    fun event(context: Context,eventId:String,map:MutableMap<String,String>){
        MobclickAgent.onEvent(context,eventId,map)
    }

    fun onExit(context: Context){
        MobclickAgent.onKillProcess(context)
    }

}