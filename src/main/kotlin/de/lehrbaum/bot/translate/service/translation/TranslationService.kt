package de.lehrbaum.bot.translate.service.translation

interface TranslationService {
	suspend fun detectLanguage(text: String, suggestions: Collection<String>): String?
	suspend fun getLanguages(): Collection<Language>
	suspend fun translate(text: String, sourceLang: String, targetLang: String): String
}

data class Language(val code: String, val name: String)
