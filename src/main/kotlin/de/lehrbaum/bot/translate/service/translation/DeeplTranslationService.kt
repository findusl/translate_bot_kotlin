package de.lehrbaum.bot.translate.service.translation

import de.lehrbaum.bot.translate.config.Secrets
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class DeeplTranslationService(
	private val httpClient: HttpClient,
	private val secrets: Secrets
) : TranslationService {
	override suspend fun detectLanguage(text: String, suggestions: Collection<String>): String? {
		throw UnsupportedOperationException("Unsupported by Deepl")
	}

	override suspend fun getLanguages(): Collection<Language> {
		throw UnsupportedOperationException("Unsupported by Deepl")
	}

	override suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
		val apiKey = secrets.deepl.apiKey

		val response = httpClient.post("https://api-free.deepl.com/v2/translate") {
			header("Authorization", "DeepL-Auth-Key $apiKey")
			contentType(ContentType.Application.FormUrlEncoded)
			setBody(
				listOf(
					"text" to text,
					"source_lang" to sourceLang,
					"target_lang" to targetLang,
					"formality" to "prefer_less",
				).formUrlEncode()
			)
		}

		val responseBody = response.body<TranslationResponse>()

		return responseBody.translations.first().text
	}

	@Serializable
	private data class TranslationResponse(val translations: List<Translation>)

	@Serializable
	private data class Translation(val detected_source_language: String, val text: String)

}
