package de.lehrbaum.bot.translate

import com.sksamuel.hoplite.ConfigLoader
import de.lehrbaum.bot.translate.config.Secrets
import de.lehrbaum.bot.translate.service.translation.TranslationService
import de.lehrbaum.bot.translate.service.translation.YandexTokenService
import de.lehrbaum.bot.translate.service.translation.YandexTranslationService
import de.lehrbaum.bot.translate.telegram.TelegramBotFactory
import de.lehrbaum.bot.translate.telegram.TranslateBotLogic
import io.ktor.client.*
import io.ktor.client.features.json.*
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
	// single<TranslationService> { FakeTranslationService() }
	single<TranslationService> { YandexTranslationService(get(), get()) }
	single { YandexTokenService(get(), get()) }
	single { setupKtorHttpClient() }
	single { TelegramBotFactory(get()) }
}

private fun setupKtorHttpClient(): HttpClient {
	return HttpClient {
		install(JsonFeature)
	}
}

private val configModule = module {
	single { ConfigLoader().loadConfigOrThrow<Secrets>("/secrets.yaml") }
}