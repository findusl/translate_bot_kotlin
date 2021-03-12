package de.lehrbaum.bot.translate

import de.lehrbaum.bot.translate.config.Secrets

class FakeTranslationService(val secrets: Secrets) {

	fun detectLanguage(text: String): String {
		return "en"
	}

	fun getLanguages(): List<String> {
		return listOf("en", "de", "fr")
	}

}
