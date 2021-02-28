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
		modules(configModule)
	}
}

internal val configModule = module {
	single { ConfigLoader().loadConfigOrThrow<Secrets>("/secrets.yaml") }
}
