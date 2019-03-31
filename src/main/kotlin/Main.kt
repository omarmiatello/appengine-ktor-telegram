package com.github.jacklt.gae.ktor.tg

import com.github.jacklt.yeelight.Yeelight
import kotlinx.coroutines.runBlocking

fun main() = startApp()

val yeelights = runBlocking { Yeelight.findDevices() }
    .also { println("Found ${it.size} yeelight!\nApp Started!") }

fun myApp(input: String): String {
    runBlocking {
        yeelights.forEach { it.toggle() }
    }

    return "Ciao $input"
}