package com.tjhello.app.abtest

import android.os.Bundle
import com.tjhello.ab.test.ABTest
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppActivity() {

    private val abTest = ABTest.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        abTest.event("ABTestDemoEvent","show")
        btEvent.setOnClickListener {
            abTest.event("ABTestDemoEvent","click")
        }
    }
}
