package de.lehrbaum.bot.translate

import de.lehrbaum.bot.translate.config.Secrets
import de.lehrbaum.bot.translate.config.Telegram
import de.lehrbaum.bot.translate.config.Yandex
import de.lehrbaum.bot.translate.service.translation.YandexTokenService
import de.lehrbaum.bot.translate.service.translation.YandexTranslationService
import de.lehrbaum.bot.translate.telegram.TranslateBotLogic
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
fun main() {
	setupKoin()
	println("Hello world")
	MainApplication().runBot()
}

@KoinApiExtension
class MainApplication : KoinComponent {

	private val telegramBot: TranslateBotLogic by inject()

	fun runBot() {
		telegramBot.startLogic()
	}
}
