package com.tjhello.ab.test

import androidx.annotation.Keep
import java.text.SimpleDateFormat
import java.util.*

/**
 * 作者:天镜baobao
 * 时间:2020/6/11  18:02
 * 说明:允许使用，但请遵循Apache License 2.0
 * 使用：
 * Copyright 2020/6/11 天镜baobao
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
@Keep
class TimeTackInfo(val type: Int) {

    companion object {
        const val TYPE_ONCE = 0 //单次时长
        const val TYPE_DAY = 1 //一天时长
    }

    var beginTime = 0L
    var useTime = 0L
    var endTime = 0L

    fun create():TimeTackInfo{
        this.beginTime = System.currentTimeMillis()
        this.endTime = beginTime
        return this
    }

    fun canUpload() : Boolean{
        val nowTime = System.currentTimeMillis()
        if(beginTime>0){
            if(type== TYPE_ONCE){
                if(nowTime-beginTime>60*1000){
                    //如果开始时间距离当前时间大于1分钟，则判定是上一次统计,则需要提交打点，并且删除
                    return true
                }
            }else{
                val beginDay = formatDate(beginTime)
                val nowDay = formatDate(nowTime)
                if(nowDay>beginDay&&beginDay!=nowDay){
                    //如果时间过了一天，则需要提交打点，并且删除
                    return true
                }
            }
        }
        return false
    }

    fun refresh():Boolean{
        val nowTime = System.currentTimeMillis()
        if(type== TYPE_DAY){
            val beginDay = formatDate(beginTime)
            val nowDay = formatDate(nowTime)
            if(nowDay>beginDay&&beginDay!=nowDay){
                //如果时间过了一天，则需要提交打点，并且删除
                update()
                return true
            }
        }
        update()
        return false
    }

    fun onResume(){
        this.endTime = System.currentTimeMillis()
        this.beginTime = this.endTime
    }

    private fun update(){
        this.endTime = System.currentTimeMillis()
        this.useTime += this.endTime-this.beginTime
        this.beginTime = this.endTime
        ABTestOld.log("[update]${if(type== TYPE_ONCE) "once" else "day"},useTime:${useTime/1000}s")
    }

    private fun formatDate(time:Long):String{
        val format = SimpleDateFormat("yyyyMMdd",Locale.getDefault())
        return format.format(Date(time))
    }
}