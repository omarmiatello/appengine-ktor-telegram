package com.github.jacklt.gae.ktor.tg

import com.github.jacklt.gae.ktor.tg.appengine.TelegramApi
import com.github.jacklt.gae.ktor.tg.config.AppConfig
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import kotlinx.serialization.json.JSON

val telegram = AppConfig.getDefault().telegram

/**
 * Entry Point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 *
 * For more information about this file: https://ktor.io/servers/configuration.html#hocon-file
 */
fun Application.main() {
    // This adds automatically Date and Server headers to each response, and would allow you to configure
    // additional headers served to each response.
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)

    install(StatusPages)

    // Registers routes
    routing {
        // For the root / route, we respond with an Html.
        // The `respondHtml` extension method is available at the `ktor-html-builder` artifact.
        // It provides a DSL for building HTML to a Writer, potentially in a chunked way.
        // More information about this DSL: https://github.com/Kotlin/kotlinx.html

        route("webhook") {
            post("telegram") {
                val request = JSON.nonstrict.parse(TelegramApi.UpdateMessageRequest.serializer(), call.receiveText())
                call.respondText(
                    JSON.stringify(
                        TelegramApi.UpdateMessageResponse.serializer(),
                        TelegramApi.UpdateMessageResponse(request.message.chat.id,
                            request.message.text.toAppResponse()
                        )
                    ),
                    ContentType.parse("application/json")
                )
            }
        }
    }
}