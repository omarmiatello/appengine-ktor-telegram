package com.github.jacklt.gae.ktor.tg.data

import com.github.jacklt.gae.ktor.tg.appengine.FirebaseDatabaseApi
import com.github.jacklt.gae.ktor.tg.appengine.fireMap
import kotlinx.serialization.map
import kotlinx.serialization.serializer


object FireDB : FirebaseDatabaseApi() {
    override val basePath = "https://gdglevante.firebaseio.com/"

    var test by fireMap(
        key = "test",
        serializer = (String.serializer() to String.serializer()).map,
        useCache = false
    )
}