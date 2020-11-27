package com.tjhello.app.abtest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tjhello.ab.test.ABTest
import kotlinx.android.synthetic.main.game_activity_layout.*

/**
 * 作者:天镜baobao
 * 时间:2020/7/21  13:59
 * 说明:允许使用，但请遵循Apache License 2.0
 * 使用：
 * Copyright 2020/7/21 天镜baobao
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
class GameActivity: AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activity_layout)
        btGameBegin.setOnClickListener {
            ABTest.startGame("test_1")
        }

        btGamePause.setOnClickListener {
            ABTest.stopGame("test_1",false)
        }

        btGameComplete.setOnClickListener {
            ABTest.stopGame("test_1",true)
        }
    }



}