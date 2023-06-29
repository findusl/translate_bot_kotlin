package de.lehrbaum.bot.translate.service.translation

/**
 * Combines Deepl with LibreTranslate:
 * - LibreTranslate is used for the simple tasks like detecting language due to deepl token limit
 * - Deepl is used for translating, because it is amazing at that
 */
class CombinedTranslationService(
	private val libreTranslateTranslationService: LibreTranslateTranslationService,
	private val deeplTranslationService: DeeplTranslationService,
) : TranslationService {
	override suspend fun detectLanguage(text: String, suggestions: Collection<String>): String? {
		return libreTranslateTranslationService.detectLanguage(text, suggestions)
	}

	override suspend fun getLanguages(): Collection<Language> {
		// Haven't found a matching endpoint for Deepl, but probably fine
		return libreTranslateTranslationService.getLanguages()
	}

	override suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
		return deeplTranslationService.translate(text, sourceLang, targetLang)
	}

}
