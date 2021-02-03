package com.tjhello.ab.test.config

class ABConfig {
    var name = ""//测试名
    var desc = ""//测试说明
    var ver = 0//测试版本号
    var onlyNew = true//仅新用户参与测试
    var abVer = 0//AB测试目标版本号
    var data = mutableListOf<String>()//变体
    var dataWeight = mutableListOf<Int>()
    var listenEvent = mutableListOf<String>()//需要监听的事件
    var parentName: String? = ""//父测试
    var parentValue: String? = ""//父测试的值

    var mergeTag = false//是否合并所有监听的事件的AB标签到值里面
    var mergeEvent = false//是否合并多有监听的事件到base打点里面

    var ctrl : ABCtrl?= null

    fun copy(ab: ABConfig): ABConfig {
        this.name = ab.name
        this.desc = ab.desc
        this.ver = ab.ver
        this.onlyNew = ab.onlyNew
        this.abVer = ab.abVer
        this.data = ab.data
        this.listenEvent = ab.listenEvent
        this.parentName = ab.parentName
        this.parentValue = ab.parentValue
        return this
    }
}