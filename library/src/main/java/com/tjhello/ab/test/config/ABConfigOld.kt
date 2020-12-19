package com.tjhello.ab.test.config

/**
 * 创建者：TJbaobao
 * 时间:2020/2/24 10:22
 * 使用:
 * 说明:
 **/
class ABConfigOld {

    var isOnlyNew = true //是否是仅限于新用户测试
    var firstVersionCode = 0//首次接入ABTest的应用版本号
    var nowVersionCode = 0//首次安装版本号大于等于这个值才是新用户。
    var name  : String = ""//测试名称
    var dataArray : Array<String> = arrayOf()
    var endDate : String ?= null
    var listenEventArray = mutableListOf<String>()
    var parentName : String ?= ""
    var parentValue : String ?= ""

}