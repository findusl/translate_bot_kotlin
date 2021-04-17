package de.lehrbaum.bot.translate

import com.charleskorn.kaml.Yaml
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
import io.ktor.client.features.json.serializer.*
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
		install(JsonFeature) {
			serializer = KotlinxSerializer()
		}
	}
}

// TODO make the folder configurable and use for logging as well
private val configModule = module {
	single {
		Yaml.default.decodeFromString(Secrets.serializer(), File(BASE_PATH, "secrets.yaml").readText())
	}
}

private const val SETTINGS_FILE_NAME = "settings.json"

private val repositoryModule = module {
	single<ChatSettingsRepository> { ChatSettingsRepositoryImpl(File(BASE_PATH, SETTINGS_FILE_NAME)) }
}
