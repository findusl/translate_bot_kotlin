package de.lehrbaum.bot.translate.service.translation

class FakeTranslationService : TranslationService {

	override suspend fun detectLanguage(text: String, suggestions: Collection<String>): String? {
		return "en"
	}

	override suspend fun getLanguages(): Collection<Language> {
		return listOf(
			Language("en", "English"),
			Language("de", "German"),
			Language("fr", "French")
		)
	}

	override suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
		return "Translated version from $sourceLang to $targetLang of $text"
	}
}
