package com.tjhello.ab.test.config

/**
 * 作者:EYEWIND@TJHello
 * 时间:2020/12/17  21:30
 */
class OLConfig {

    private val abList = mutableListOf<ABConfig>()
    private val fixed = mutableMapOf<String, Fixed>()
    val log = 0//0不生效，1打开日志，2关闭日志

    inner class Fixed{
        var onlyNew = true
        var value = ""
    }

    fun copy(olConfig: OLConfig, function:(ab: ABConfig)->Unit){
        olConfig.fixed.forEach {
            this.fixed[it.key] = it.value
        }
        olConfig.abList.forEach {ab->
            function(ab)
        }
    }

    fun addTest(ab: ABConfig){
        abList.add(ab)
    }

    fun findTest(name:String): ABConfig?{
        return abList.find {
            return@find it.name == name
        }
    }

    fun getFixedValue(name: String): Fixed?{
        if(fixed.containsKey(name)){
            return fixed[name]
        }
        return null
    }

    fun fixedValue(name: String,value:String,onlyNew:Boolean=false){
        fixed[name] = Fixed().apply {
            this.value = value
            this.onlyNew = onlyNew
        }
    }

    fun allFixedName():Set<String>{
        return fixed.keys
    }

    fun allABConfig():MutableList<ABConfig>{
        return abList
    }
}