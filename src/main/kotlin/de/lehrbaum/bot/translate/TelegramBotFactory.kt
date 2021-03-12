package de.lehrbaum.bot.translate

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.logging.LogLevel.All
import de.lehrbaum.bot.translate.config.Secrets

class TelegramBotFactory(private val secrets: Secrets) {

	fun generateBot(setupDispatch: Dispatcher.() -> Unit): Bot =
		bot {
			token = secrets.telegram.accessToken
			logLevel = All()
			dispatch(setupDispatch)
		}
}
