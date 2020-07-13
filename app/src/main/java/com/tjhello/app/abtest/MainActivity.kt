package com.tjhello.app.abtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tjhello.ab.test.ABTest
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val abTest = ABTest.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btEvent.setOnClickListener {
            abTest.event("test", mutableMapOf("data" to "number"))
        }
    }

    override fun onPause() {
        super.onPause()
        ABTest.onPause()
    }

    override fun onResume() {
        super.onResume()
        ABTest.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        ABTest.onExit(this)
    }
}
