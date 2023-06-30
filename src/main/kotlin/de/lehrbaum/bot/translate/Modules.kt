package de.lehrbaum.bot.translate

import com.charleskorn.kaml.Yaml
import de.lehrbaum.bot.translate.KoinModules.applicationModule
import de.lehrbaum.bot.translate.KoinModules.configModule
import de.lehrbaum.bot.translate.KoinModules.repositoryModule
import de.lehrbaum.bot.translate.config.Secrets
import de.lehrbaum.bot.translate.repository.ChatSettingsRepository
import de.lehrbaum.bot.translate.repository.ChatSettingsRepositoryImpl
import de.lehrbaum.bot.translate.service.translation.CombinedTranslationService
import de.lehrbaum.bot.translate.service.translation.DeeplTranslationService
import de.lehrbaum.bot.translate.service.translation.LibreTranslateTranslationService
import de.lehrbaum.bot.translate.telegram.TranslateBotLogic
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
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

object KoinModules {
	val applicationModule = module {
		single { TranslateBotLogic(get(), get(), get()) }
		single { CombinedTranslationService(get(), get()) }
		single { DeeplTranslationService(get(), get()) }
		single { LibreTranslateTranslationService(get(), get()) }
		single { setupKtorHttpClient() }
	}

	private fun setupKtorHttpClient(): HttpClient {
		return HttpClient {
			install(ContentNegotiation) {
				json()
			}
		}
	}

	private const val SECRETS_FILE_NAME = "secrets.yaml"

	// FUTURE make the folder configurable and use for logging as well
	val configModule = module {
		single {
			val filePath = File(BASE_PATH, SECRETS_FILE_NAME)
			Yaml.default.decodeFromString(Secrets.serializer(), filePath.readText())
		}
	}

	private const val SETTINGS_FILE_NAME = "settings.json"

	val repositoryModule = module {
		single<ChatSettingsRepository> { ChatSettingsRepositoryImpl(File(BASE_PATH, SETTINGS_FILE_NAME)) }
	}
}
