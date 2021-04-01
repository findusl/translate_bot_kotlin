package de.lehrbaum.bot.translate

import de.lehrbaum.bot.translate.telegram.TranslateBotLogic
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
