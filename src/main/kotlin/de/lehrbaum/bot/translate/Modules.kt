package de.lehrbaum.bot.translate

import com.sksamuel.hoplite.ConfigLoader
import de.lehrbaum.bot.translate.config.Secrets
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal fun setupKoin() {
	startKoin {
		// use Koin logger
		printLogger()
		// declare modules
		modules(configModule, applicationModule)
	}
}

private val applicationModule = module {
	single { TranslateBotLogic(get(), get()) }
	single { FakeTranslationService(get()) }
	single { TelegramBotFactory(get()) }
}

private val configModule = module {
	single { ConfigLoader().loadConfigOrThrow<Secrets>("/secrets.yaml") }
}
