package com.tjhello.app.abtest

import android.app.Application
import com.tjhello.ab.test.config.ABConfig
import com.tjhello.ab.test.ABTest
import com.tjhello.ab.test.RemoteConfig

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
        ABTest.isOpenLogcat = true

        //本地配置ABTest
        ABTest.init(this, true)
                .addTest(ABConfig().apply {//添加一项ABTest
                    this.name = "ABTestDemo"//测试名称
                    this.ver = 0//AB测试版本，默认0，如果同一个测试不同版本，则修改这个数值
                    this.abVer = 10001//当前测试对应的应用版本号
                    this.data = mutableListOf("1","2")//支持任意多维度的数据
                    this.listenEvent = mutableListOf("ABTestDemoEvent")//需要纳入AB的事件,留空则监听所有事件
                    this.mergeEvent = false//合并事件(测试中)
                    this.mergeTag = true//合并标签eg:data_A=value1,data_B=value2  ->  data = value1_A,data = value2_B
                    this.onlyNew = true//仅将新增用户纳入测试结果
                })

        //通过Firebase-RemoteConfig在线配置ABTest（需要另外接入该库）
        ABTest.init(this, true)
//        RemoteConfig.init(){
//            if(it){
//                ABTest.addTestByRemoteConfig()
//            }
//        }

        //通过自己的服务器在线配置ABTest
        ABTest.init(this, true)
        //ABTest.addTestByJsonConfig(xx)
        //ABTest.addTestByInfoConfig()
    }

}