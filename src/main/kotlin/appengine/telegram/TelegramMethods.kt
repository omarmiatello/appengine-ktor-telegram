package com.github.jacklt.gae.ktor.tg.appengine.telegram

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

sealed class TelegramMethod

@Serializable
data class SendMessage(
    val chat_id: String,
    val text: String,
    @Optional val parse_mode: String? = null,
    @Optional val disable_web_page_preview: Boolean? = null,
    @Optional val message_id: Int? = null,
    @Optional val reply_markup: InlineKeyboardMarkup? = null
) : TelegramMethod()

@Serializable
data class EditMessageText(
    val chat_id: String,
    val text: String,
    @Optional val parse_mode: String? = null,
    @Optional val disable_web_page_preview: Boolean? = null,
    @Optional val message_id: Int? = null,
    @Optional val reply_markup: InlineKeyboardMarkup? = null
) : TelegramMethod()

@Serializable
data class ForwardMessage(
    val chat_id: String,
    val from_chat_id: String,
    val message_id: Int,
    @Optional val disable_notification: Boolean? = null
) : TelegramMethod()

@Serializable
data class DeleteMessage(
    val chat_id: String,
    val message_id: Int
) : TelegramMethod()

@Serializable
data class UpdateMessageText(
    val chat_id: Int,
    val text: String,
    @Optional val parse_mode: String? = null,
    @Optional val disable_web_page_preview: Boolean? = null,
    @Optional val message_id: Int? = null,
    @Optional val reply_markup: InlineKeyboardMarkup? = null
) : TelegramMethod()
