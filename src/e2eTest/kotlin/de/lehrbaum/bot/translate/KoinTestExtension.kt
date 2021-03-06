package de.lehrbaum.bot.translate

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * Setup Koin and tear down at the end of test.
 * Also override some of the modules for testing.
 */
class KoinTestExtension : BeforeEachCallback, AfterEachCallback {
	override fun beforeEach(context: ExtensionContext?) {
		setupKoin()
		loadKoinModules(module { single(override = true) { generateTestHttpClient() } })
	}

	private fun generateTestHttpClient(): HttpClient {
		return HttpClient {
			install(JsonFeature)
			install(Logging) {
				level = LogLevel.ALL
				// TODO use custom logger here based on java util logging
			}
		}
	}

	override fun afterEach(context: ExtensionContext?) {
		stopKoin()
	}
}
