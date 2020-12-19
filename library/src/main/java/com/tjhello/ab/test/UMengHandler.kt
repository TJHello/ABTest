package com.tjhello.ab.test

import android.content.Context
import com.umeng.analytics.MobclickAgent
import java.util.concurrent.Executors


/**
 * 作者:TJbaobao
 * 时间:2019/1/22  18:18
 * 说明:
 * 使用：
 */
object UMengHandler {

    private val threadPool = Executors.newFixedThreadPool(3)

    @JvmStatic
    fun event(context: Context,eventId:String,map:MutableMap<String,String>){
        threadPool.submit {
            MobclickAgent.onEvent(context,eventId,map)
        }
    }


    @JvmStatic
    fun eventObject(context: Context,eventId:String,map:MutableMap<String,out Any>){
        threadPool.submit {
            MobclickAgent.onEventObject(context,eventId,map)
        }
    }

    fun onExit(context: Context){
        MobclickAgent.onKillProcess(context)
    }

}