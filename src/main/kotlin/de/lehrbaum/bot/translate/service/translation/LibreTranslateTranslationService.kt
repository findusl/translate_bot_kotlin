package de.lehrbaum.bot.translate.service.translation

import de.lehrbaum.bot.translate.config.Secrets
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class LibreTranslateTranslationService(
	private val httpClient: HttpClient,
	private val secrets: Secrets
) : TranslationService {
	override suspend fun detectLanguage(text: String, suggestions: Collection<String>): String? {
		val response: List<DetectionResponse> = httpClient.post("${secrets.libreTranslate.baseUrl}/detect") {
			contentType(ContentType.Application.FormUrlEncoded)
			setBody(listOf("q" to text).formUrlEncode())
		}.body()

		return response.firstOrNull { it.confidence > 50.0 || (it.language in suggestions && it.confidence > 30.0) }?.language
	}

	@Serializable
	private data class DetectionResponse(
		val confidence: Double,
		val language: String
	)

	override suspend fun getLanguages(): Collection<Language> {
		val response = httpClient.get("${secrets.libreTranslate.baseUrl}/languages")
		val body = response.body<List<LanguageResponse>>()
		return body.map { Language(it.code, it.name) }
	}

	override suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
		throw UnsupportedOperationException("Unsupported by Deepl")
	}

	@Serializable
	private data class LanguageResponse(val code: String, val name: String, val targets: List<String>)
}