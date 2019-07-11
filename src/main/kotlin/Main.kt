package com.github.jacklt.gae.ktor.tg

import com.github.jacklt.gae.ktor.tg.appengine.isAppEngine
import com.github.jacklt.gae.ktor.tg.appengine.telegram.Message
import com.github.jacklt.gae.ktor.tg.data.FireDB
import com.github.jacklt.gae.ktor.tg.data.LocalFireDB

fun main() = startApp()

fun myApp(input: String, message: Message): String {
    val dbTest = if (isAppEngine) FireDB.test else LocalFireDB.test

    val name = dbTest["name"]

    return "Ciao $input by $name"
}