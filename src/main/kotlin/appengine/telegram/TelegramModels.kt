package com.github.jacklt.gae.ktor.tg.appengine.telegram

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

sealed class TelegramModel

@Serializable
data class Message(
    val message_id: Int,
    val chat: ChatResponse,
    @Optional val text: String? = null
) : TelegramModel()


@Serializable
data class ChatResponse(
    val id: Int
) : TelegramModel()

@Serializable
data class InlineKeyboardMarkup(
    val inline_keyboard: List<List<InlineKeyboardButton>>
) : TelegramModel()

@Serializable
data class InlineKeyboardButton(
    val text: String,
    val url: String
) : TelegramModel()

@Serializable
data class WebhookUpdateMessage(
    val update_id: Int,
    @Optional val message: Message? = null,
    @Optional val inline_query: InlineQuery? = null
) : TelegramModel()

@Serializable
data class User(
    val id: Int,
    val is_bot: Boolean,
    val first_name: String,
    @Optional val last_name: String? = null,
    @Optional val username: String? = null,
    @Optional val language_code: String? = null
) : TelegramModel()

@Serializable
data class InlineQuery(
    val id: String,
    val from: User,
    val query: String,
    val offset: String
) : TelegramModel()
