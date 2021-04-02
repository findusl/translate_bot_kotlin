package de.lehrbaum.bot.translate.extensions

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.HandleCommand
import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun CommandHandlerEnvironment.replyToMessage(text: String) {
	bot.sendMessage(ChatId.fromId(message.chat.id), text, replyToMessageId = message.messageId)
}

fun TextHandlerEnvironment.replyToMessage(text: String) {
	bot.sendMessage(ChatId.fromId(message.chat.id), text, replyToMessageId = message.messageId)
}

inline fun Dispatcher.consumeCommand(command: String, crossinline handleCommand: HandleCommand) {
	command(command) {
		update.consume()
		handleCommand()
	}
}

inline fun Dispatcher.consumeCommand(
	command: String,
	scope: CoroutineScope,
	crossinline handleCommand: suspend CommandHandlerEnvironment.() -> Unit
) {
	command(command) {
		update.consume()
		scope.launch {
			handleCommand()
		}
	}
}
