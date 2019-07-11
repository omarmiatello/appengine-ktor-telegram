package com.github.jacklt.gae.ktor.tg.data

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.typeTokenOf
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object LocalFireDB {
    init {
        FirebaseApp.initializeApp(
            FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setDatabaseUrl("https://gdglevante.firebaseio.com/")
                .build()
        )
    }

    var test by localFireDB<Map<String, String>>("test")
}

inline fun <reified T> localFireDB(key: String) =
    object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            return runBlocking {
                Gson().run {
                    val value = FirebaseDatabase.getInstance().getReference(key).singleValue().value
                    fromJson<T>(toJson(value), typeTokenOf<T>())
                }
            }
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            FirebaseDatabase.getInstance().getReference(key).setValueAsync(value).get()
        }
    }

suspend fun DatabaseReference.singleValue(): DataSnapshot = suspendCancellableCoroutine { cont ->
    addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(error: DatabaseError?) {
            cont.resumeWithException(error!!.toException())
        }

        override fun onDataChange(snapshot: DataSnapshot?) {
            cont.resume(snapshot!!)
        }
    })
}