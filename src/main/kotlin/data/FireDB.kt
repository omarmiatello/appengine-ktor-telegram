package com.github.jacklt.gae.ktor.tg.data

import com.github.jacklt.gae.ktor.tg.appengine.*
import com.github.jacklt.gae.ktor.tg.appengine.telegram.Message
import kotlinx.serialization.map
import kotlinx.serialization.serializer


object FireDB : FirebaseDatabaseApi() {
    override val basePath = "https://gdglevante.firebaseio.com/"

    var testString by fireProperty(String.serializer())
    var lastMessage by fireProperty(Message.serializer())
    var testMap by fireMap((String.serializer() to String.serializer()).map)

    // TODO fix local only
    fun getStringTestMap(key: String) = localGet<String>("testMap/$key")

    // TODO fix local only
    fun addStringTestMap(key: String, value: String) = localSet("testMap/$key", value)
}