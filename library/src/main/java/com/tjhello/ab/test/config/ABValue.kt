package com.tjhello.ab.test.config

/**
 * 作者:EYEWIND@TJHello
 * 时间:2020/12/18  15:27
 */
class ABValue {

    constructor()

    constructor(position:Int,value:String):super(){
        this.position = position
        this.value = value
    }

    constructor(value:String):super(){
        this.value = value
    }

    var position = -1
    var value = ""



}