package de.lehrbaum.bot.translate

import de.lehrbaum.bot.translate.telegram.TranslateBotLogic
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.logging.*


@KoinApiExtension
fun main() {
	setupLogger()
	setupKoin()
	println("Hello world")
	MainApplication().runBot()
}

private val mainLogger = Logger.getLogger(Main::class.java.packageName)

private fun setupLogger() {
	mainLogger.useParentHandlers = false
	mainLogger.level = Level.ALL
	val handler = object : StreamHandler(System.out, SimpleFormatter()) {
		@Synchronized
		override fun publish(record: LogRecord?) {
			super.publish(record)
			flush() // need to autoflush
		}
	}
	handler.level = Level.ALL
	// TODO make configurable and log to file and system err etc
	mainLogger.addHandler(handler)
}

/** Just used to reference the package */
private class Main

@KoinApiExtension
class MainApplication : KoinComponent {

	private val telegramBot: TranslateBotLogic by inject()

	fun runBot() {
		telegramBot.startLogic()
	}
}
