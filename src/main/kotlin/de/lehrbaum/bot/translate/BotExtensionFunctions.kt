package de.lehrbaum.bot.translate

import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId

fun CommandHandlerEnvironment.replyToMessage(text: String) {
	bot.sendMessage(ChatId.fromId(message.chat.id), text, replyToMessageId = message.messageId)
}
