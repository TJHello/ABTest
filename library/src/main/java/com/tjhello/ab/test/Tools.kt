package com.tjhello.ab.test

import android.content.Context
import android.content.SharedPreferences.Editor
import android.util.DisplayMetrics
import android.view.WindowManager
import java.util.*
import java.util.concurrent.Executors

/**
 * 创建者：EYEWIND@TJHello
 * 时间:2020/2/24 10:30
 * 使用:
 * 说明:
 **/
internal class Tools(private val context: Context) {

    companion object{
        private val threadPool = Executors.newFixedThreadPool(1)

        @JvmStatic
        fun containsClass(name: String):Boolean{
            return try {
                Class.forName(name)
                true
            }catch (e: ClassNotFoundException){
                ABTestOld.log("Not imported:$name")
                false
            }
        }
    }

    private val pref = context.getSharedPreferences("ab-test", 0)

    fun <T>getSharedPreferencesValue(key: String?, defValue: T?): T? {
        if (defValue == null || defValue is String) {
            return pref.getString(key, defValue as? String) as? T?
        } else if (defValue is Int) {
            return pref.getInt(key, defValue) as? T?
        } else if (defValue is Float) {
            return pref.getFloat(key, defValue) as? T?
        } else if (defValue is Long) {
            return pref.getLong(key, defValue) as? T?
        } else if (defValue is Boolean) {
            return pref.getBoolean(key, defValue) as? T?
        }
        return null
    }

    fun setSharedPreferencesValue(key: String?, value: Any?) {
        threadPool.submit {
            val editor: Editor = pref.edit()
            if (value == null || value is String) {
                editor.putString(key, value as String?)
            } else if (value is Int) {
                editor.putInt(key, (value as Int?)!!)
            } else if (value is Float) {
                editor.putFloat(key, (value as Float?)!!)
            } else if (value is Long) {
                editor.putLong(key, (value as Long?)!!)
            } else if (value is Boolean) {
                editor.putBoolean(key, (value as Boolean?)!!)
            }
            editor.commit()
        }
    }

    fun getScreenWidth():Int{
        val dm = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }

    fun getScreenHeight():Int{
        val dm = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(dm)
        return dm.heightPixels
    }

}