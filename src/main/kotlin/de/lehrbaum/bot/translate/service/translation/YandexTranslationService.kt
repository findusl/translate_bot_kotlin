package de.lehrbaum.bot.translate.service.translation

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*


class YandexTranslationService(private val httpClient: HttpClient, private val yandexTokenService: YandexTokenService) :
	TranslationService {

	override suspend fun detectLanguage(text: String, suggestions: Collection<String>): String {
		val iamToken = yandexTokenService.requestIAMToken()

		val response: DetectLanguageResponse = httpClient.post {
			url("https://translate.api.cloud.yandex.net/translate/v2/detect")
			header(HttpHeaders.Authorization, "Bearer $iamToken")
			contentType(ContentType.Application.Json)
			body = DetectLanguageRequest(text, suggestions)
		}
		return response.languageCode
	}

	override suspend fun getLanguages(): Collection<Language> {
		val iamToken = yandexTokenService.requestIAMToken()

		val response: GetLanguagesResponse = httpClient.post {
			url("https://translate.api.cloud.yandex.net/translate/v2/languages")
			header(HttpHeaders.Authorization, "Bearer $iamToken")
		}

		return response.languages.filter {
			it.name != null
		}.map {
			Language(it.code, it.name!!)
		}
	}

	override suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
		val iamToken = yandexTokenService.requestIAMToken()
		val body = TranslateTextRequest(sourceLang, targetLang, listOf(text))

		val response: TranslateTextResponse = httpClient.post {
			url("https://translate.api.cloud.yandex.net/translate/v2/languages")
			header(HttpHeaders.Authorization, "Bearer $iamToken")
			contentType(ContentType.Application.Json)
			this.body = body
		}
		return response.translations.first().text
	}
}

private data class DetectLanguageRequest(val text: String, val languageCodeHints: Collection<String>)
private data class DetectLanguageResponse(val languageCode: String)

private data class GetLanguagesResponse(val languages: Collection<GetLanguagesLanguageResponse>)
private data class GetLanguagesLanguageResponse(val code: String, val name: String?)

private data class TranslateTextRequest(
	val sourceLanguageCode: String,
	val targetLanguageCode: String,
	val texts: Collection<String>,
	val format: String = "PLAIN_TEXT"
)

private data class TranslateTextResponse(val translations: Collection<Translation>) {
	data class Translation(val text: String)
}
