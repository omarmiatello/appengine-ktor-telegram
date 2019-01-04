package com.github.jacklt.gae.ktor.tg

import kotlinx.io.PrintWriter
import kotlinx.io.StringWriter

val Exception.stackTraceString get() = StringWriter().also { printStackTrace(PrintWriter(it)) }.toString()

fun startApp() {
    while (true) {
        val input = readLine().orEmpty()
        val message = input.toAppResponse()
        println(message)
    }
}

fun String.toAppResponse(): String {
    return try {
        myApp(this)
    } catch (e: Exception) {
        "C'è stato un errore: ```\n${e.stackTraceString}\n```"
    }.ifBlank {
        "Non so cosa rispondere... 🙄"
    }
}
