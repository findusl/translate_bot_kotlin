package de.lehrbaum.bot.translate

import com.sksamuel.hoplite.ConfigLoader
import de.lehrbaum.bot.translate.config.Secrets
import de.lehrbaum.bot.translate.repository.ChatSettingsRepository
import de.lehrbaum.bot.translate.repository.ChatSettingsRepositoryImpl
import de.lehrbaum.bot.translate.service.translation.TranslationService
import de.lehrbaum.bot.translate.service.translation.YandexTokenService
import de.lehrbaum.bot.translate.service.translation.YandexTranslationService
import de.lehrbaum.bot.translate.telegram.TelegramBotFactory
import de.lehrbaum.bot.translate.telegram.TranslateBotLogic
import io.ktor.client.*
import io.ktor.client.features.json.*
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File

fun setupKoin() {
	startKoin {
		// use Koin logger
		printLogger()
		// declare modules
		modules(configModule, applicationModule, repositoryModule)
	}
}

private val applicationModule = module {
	single { TranslateBotLogic(get(), get(), get()) }
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

private val repositoryModule = module {
	single<ChatSettingsRepository> { ChatSettingsRepositoryImpl(File("/data/settings.json")) }
}
