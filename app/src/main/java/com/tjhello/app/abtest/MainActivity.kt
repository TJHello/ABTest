package com.tjhello.app.abtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tjhello.ab.test.ABTest
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val abTest = ABTest(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btEvent.setOnClickListener {
            abTest.event("test", mutableMapOf("data" to "number"))
        }
    }
}
