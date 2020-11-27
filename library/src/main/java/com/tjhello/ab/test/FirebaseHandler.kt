package com.tjhello.ab.test

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * 作者:天镜baobao
 * 时间:2020/3/23  15:47
 * 说明:允许使用，但请遵循Apache License 2.0
 * 使用：
 * Copyright 2020/3/23 天镜baobao
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
object FirebaseHandler {

    fun event(context: Context,eventId:String,map: MutableMap<String,String>){
        val bundle = Bundle()
        val keySet = map.keys
        for(key in keySet){
            val value = map[key]
            if(value!=null){
                bundle.putString(key,value)
            }
        }
        FirebaseAnalytics.getInstance(context).logEvent(eventId,bundle)
    }

    fun eventNum(context: Context,eventId:String,map: MutableMap<String,Int>){
        val bundle = Bundle()
        val keySet = map.keys
        for(key in keySet){
            val value = map[key]
            if(value!=null){
                bundle.putInt(key,value)
            }
        }
        FirebaseAnalytics.getInstance(context).logEvent(eventId,bundle)
    }

}