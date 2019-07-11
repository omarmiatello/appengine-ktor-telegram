package com.github.jacklt.gae.ktor.tg

import com.github.jacklt.gae.ktor.tg.appengine.telegram.Message
import com.github.jacklt.gae.ktor.tg.data.FireDB

fun main() = startApp()

// Firebase DB
// Write example:
//      FireDB.testMap = mapOf("name" to "Vale")
//      FireDB.testString = "test 🥳"
//      FireDB.lastMessage = message
// Read example:
//      FireDB.testString

fun myApp(message: Message): String {
    val input = message.text.orEmpty()
    val name = FireDB.testMap["name"]
    return "Ciao $input by $name"
}