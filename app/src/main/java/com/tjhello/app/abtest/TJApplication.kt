package com.tjhello.app.abtest

import android.app.Application
import com.tjhello.ab.test.ABConfig
import com.tjhello.ab.test.ABTest

/**
 * 作者:天镜baobao
 * 时间:2020/3/20  16:01
 * 说明:允许使用，但请遵循Apache License 2.0
 * 使用：
 * Copyright 2020/3/20 天镜baobao
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
class TJApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ABTest.isDebug = true
        ABTest.init(this, true)
            .addTest(this,ABConfig().apply {
            this.dataArray = arrayOf("0","1")
            this.firstVersionCode = 1
            this.isOnlyNew = true
            this.name = "NewUI2"
            this.nowVersionCode = 2 })
            .startTimeTack()
    }

}