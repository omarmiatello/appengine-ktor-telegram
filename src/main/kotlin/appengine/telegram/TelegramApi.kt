package com.github.jacklt.gae.ktor.tg.appengine.telegram

import com.github.jacklt.gae.ktor.tg.config.AppConfig
import com.github.jacklt.gae.ktor.tg.utils.parseNotNull
import com.github.jacklt.gae.ktor.tg.utils.toJsonContent
import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpContent
import com.google.api.client.http.HttpRequestFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

object TelegramApi {
    private val config = AppConfig.getDefault().telegram
    private val basePath = "https://api.telegram.org/bot${config.apiKey}"
    private val httpTransport = UrlFetchTransport.getDefaultInstance()
    private fun requestFactory(): HttpRequestFactory = httpTransport.createRequestFactory()

    private fun <T> telegram(path: String, body: HttpContent, response: KSerializer<T>) =
        requestFactory().buildPostRequest(GenericUrl(path), body).execute()
            .parseNotNull(TelegramResponse.serializer(response))

    @Serializable
    data class TelegramResponse<T>(val ok: Boolean, val result: T)

    fun sendMessage(
        chatId: String,
        text: String,
        parseMode: ParseMode = ParseMode.NONE,
        disableWebPagePreview: Boolean = false,
        button: List<List<InlineKeyboardButton>>? = null
    ) = telegram(
        "$basePath/sendMessage",
        SendMessage(
            chat_id = chatId,
            text = text,
            parse_mode = parseMode.str,
            disable_web_page_preview = disableWebPagePreview,
            reply_markup = button?.let { InlineKeyboardMarkup(it) }
        ).toJsonContent(SendMessage.serializer()),
        Message.serializer())

    fun editMessageText(
        chatId: String,
        messageId: Int,
        text: String,
        parseMode: ParseMode = ParseMode.NONE,
        disableWebPagePreview: Boolean = false,
        button: List<List<InlineKeyboardButton>>? = null
    ) = telegram(
        "$basePath/editMessageText",
        EditMessageText(
            text = text,
            chat_id = chatId,
            message_id = messageId,
            parse_mode = parseMode.str,
            disable_web_page_preview = disableWebPagePreview,
            reply_markup = button?.let { InlineKeyboardMarkup(it) }
        ).toJsonContent(EditMessageText.serializer()),
        Message.serializer())

    fun forwardMessage(
        chatId: String,
        fromChatId: String,
        messageId: Int,
        disableNotification: Boolean? = null
    ) = telegram(
        "$basePath/forwardMessage",
        ForwardMessage(
            chat_id = chatId,
            from_chat_id = fromChatId,
            message_id = messageId,
            disable_notification = disableNotification
        ).toJsonContent(ForwardMessage.serializer()),
        Message.serializer()
    )

    fun deleteMessage(chatId: String, messageId: Int) = telegram(
        "$basePath/deleteMessage",
        DeleteMessage(chatId, messageId).toJsonContent(DeleteMessage.serializer()),
        Boolean.serializer()
    )

    enum class ParseMode(val str: kotlin.String?) {
        NONE(null),
        MARKDOWN("Markdown"),
        HTML("HTML")
    }
}