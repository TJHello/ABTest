package com.tjhello.app.abtest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tjhello.ab.test.ABTest
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btEvent.setOnClickListener {
            startActivity(Intent(this,GameActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ABTest.onExit(this)
    }
}
