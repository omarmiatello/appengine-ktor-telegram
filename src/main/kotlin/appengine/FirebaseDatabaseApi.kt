package com.github.jacklt.gae.ktor.tg.appengine

import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpResponseException
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


@Serializable
class PutResponse(val name: String)

// The following methods are to illustrate making various calls to
// Firebase from App Engine Standard
abstract class FirebaseDatabaseApi {
    abstract val basePath: String

    private val httpTransport = UrlFetchTransport.getDefaultInstance()

    private fun requestFactory(): HttpRequestFactory = httpTransport.createRequestFactory(
        GoogleCredential.getApplicationDefault().createScoped(
            listOf(
                "https://www.googleapis.com/auth/firebase.database",
                "https://www.googleapis.com/auth/userinfo.email"
            )
        )
    )

    init {
        if (!isAppEngine) {
            FirebaseApp.initializeApp(
                FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setDatabaseUrl(basePath)
                    .build()
            )
        }
    }

    fun get(path: String) = requestFactory()
        .buildGetRequest(GenericUrl(path))
        .execute()
        .apply { if (!isSuccessStatusCode) throw HttpResponseException(this) }

    private fun <T> post(path: String, obj: T, serializer: SerializationStrategy<T>) = requestFactory()
        .buildPostRequest(
            GenericUrl(path),
            obj.toJsonContent(serializer)
        )
        .execute()
        .parse(PutResponse.serializer())?.name

    private fun <T> put(path: String, obj: T, serializer: SerializationStrategy<T>) = requestFactory()
        .buildPutRequest(
            GenericUrl(path),
            obj.toJsonContent(serializer)
        )
        .execute()
        .apply { if (!isSuccessStatusCode) throw HttpResponseException(this) }

    private fun <T> patch(path: String, obj: T, serializer: SerializationStrategy<T>) = requestFactory()
        .buildPatchRequest(
            GenericUrl(path),
            obj.toJsonContent(serializer)
        )
        .execute()
        .apply { if (!isSuccessStatusCode) throw HttpResponseException(this) }

    private fun delete(path: String): HttpResponse = requestFactory()
        .buildDeleteRequest(GenericUrl(path))
        .execute()
        .apply { if (!isSuccessStatusCode) throw HttpResponseException(this) }


    inline operator fun <reified T> get(path: String, resp: KSerializer<T>) =
        get("$basePath$path.json").parse(resp)

    operator fun <T> set(path: String, serializer: KSerializer<T>, obj: T) =
        put("$basePath$path.json", obj, serializer)

    protected fun <T> addItem(path: String, obj: T, serializer: KSerializer<T>) =
        post("$basePath$path.json", obj, serializer)

    protected fun <T> update(path: String, map: Map<String, T>, serializer: KSerializer<T>) =
        patch("$basePath$path.json", map, (String.serializer() to serializer).map)

    protected fun deletePath(path: String) = delete("$basePath$path.json")
}

inline fun <reified T> fireProperty(serializer: KSerializer<T>, key: String? = null, useCache: Boolean = true) =
    if (isAppEngine) {
        object : ReadWriteProperty<FirebaseDatabaseApi, T?> {
            override fun getValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>): T? {
                return if (useCache) {
                    val string = appEngineCacheFast.getOrPut("${thisRef.javaClass.canonicalName}.${property.name}") {
                        json.stringify(serializer, thisRef[key ?: property.name, serializer]!!)
                    }
                    json.parse(serializer, string)
                } else {
                    thisRef[key ?: property.name, serializer]
                }
            }

            override fun setValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>, value: T?) {
                if (useCache && value != null) {
                    appEngineCacheFast["${thisRef.javaClass.canonicalName}.${property.name}"] =
                        json.stringify(serializer, value)
                }
                thisRef[key ?: property.name, serializer] =
                    value ?: throw IllegalArgumentException("Use deletePath() insted of set `null` in path: $key")
            }
        }
    } else localFireDB<T?>(key)

inline fun <reified T> fireList(serializer: KSerializer<Collection<T>>, key: String? = null, useCache: Boolean = true) =
    if (isAppEngine) {
        object : ReadWriteProperty<FirebaseDatabaseApi, Collection<T>> {
            override fun getValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>): Collection<T> {
                return if (useCache) {
                    val string = appEngineCacheFast.getOrPut("${thisRef.javaClass.canonicalName}.${property.name}") {
                        json.stringify(serializer, thisRef[key ?: property.name, serializer].orEmpty())
                    }
                    json.parse(serializer, string)
                } else {
                    thisRef[key ?: property.name, serializer].orEmpty()
                }
            }

            override fun setValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>, value: Collection<T>) {
                if (useCache) {
                    appEngineCacheFast["${thisRef.javaClass.canonicalName}.${property.name}"] =
                        json.stringify(serializer, value)
                }
                thisRef[key ?: property.name, serializer] = value
            }
        }
    } else localFireDB<Collection<T>>(key)

inline fun <reified T> fireMap(serializer: KSerializer<Map<String, T>>, key: String? = null, useCache: Boolean = true) =
    if (isAppEngine) {
        object : ReadWriteProperty<FirebaseDatabaseApi, Map<String, T>> {
            override fun getValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>): Map<String, T> {
                return if (useCache) {
                    val string = appEngineCacheFast.getOrPut("${thisRef.javaClass.canonicalName}.${property.name}") {
                        json.stringify(serializer, thisRef[key ?: property.name, serializer].orEmpty())
                    }
                    json.parse(serializer, string)
                } else {
                    thisRef[key ?: property.name, serializer].orEmpty()
                }
            }

            override fun setValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>, value: Map<String, T>) {
                if (useCache) {
                    appEngineCacheFast["${thisRef.javaClass.canonicalName}.${property.name}"] =
                        json.stringify(serializer, value)
                }
                thisRef[key ?: property.name, serializer] = value
            }
        }
    } else localFireDB<Map<String, T>>(key)

inline fun <reified T> localFireDB(key: String?) =
    object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            return localGet(key ?: property.name)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            localSet(key ?: property.name, value)
        }
    }

inline fun <reified T> localGet(key: String): T {
    return runBlocking {
        Gson().run {
            val value = FirebaseDatabase.getInstance().getReference(key).singleValue().value
            fromJson<T>(toJson(value), typeTokenOf<T>())
        }
    }
}

inline fun <reified T> localSet(key: String, value: T) {
    FirebaseDatabase.getInstance().getReference(key).setValueAsync(value).get()
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