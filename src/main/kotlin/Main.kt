package com.github.jacklt.gae.ktor.tg

fun main() = startApp()

fun myApp(input: String): String {
    var res = 0.0
    var currOp: String? = null
    input.split(" ").forEach {
        val num = it.toDoubleOrNull()
        if (num != null) {
            if (currOp == null) {
                res = num
            } else {
                res = when (currOp) {
                    "+" -> res + num
                    "-" -> res - num
                    "*" -> res * num
                    "/" -> res / num
                    else -> error("Unknown operator $currOp")
                }
            }
        } else {
            if (it in "+-*/") {
                currOp = it
            } else {
                error("Unknown operator $it")
            }
        }
    }

    return "$input = $res"
}
