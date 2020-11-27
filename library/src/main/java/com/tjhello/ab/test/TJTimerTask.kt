package com.tjhello.ab.test

import java.util.*

/**
 * 作者:天镜baobao
 * 时间:2020/6/12  13:55
 * 说明:允许使用，但请遵循Apache License 2.0
 * 使用：
 * Copyright 2020/6/12 天镜baobao
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
class TJTimerTask(private val runnable: Runnable) {

    private var timer = Timer()
    private var timerTask = MyTimerTask()
    private var isCancel = true

    init {

    }

    fun start(){
        start(0L)
    }

    fun start(delay:Long){
        stop()
        timer = Timer()
        timerTask = MyTimerTask()
        timer.schedule(timerTask,delay)
    }

    fun start(delay: Long,period:Long){
        stop()
        isCancel = false
        timer = Timer()
        timerTask = MyTimerTask()
        timer.schedule(timerTask,delay,period)
    }

    fun stop(){
        if(!isCancel){
            isCancel = true
            timer.cancel()
            timerTask.cancel()
        }
    }

    private inner class MyTimerTask : TimerTask() {
        override fun run() {
            if (!isCancel) {
                runnable.run()
            }
        }
    }

}