package de.lehrbaum.bot.translate.service.translation

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder


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
			url("https://translate.api.cloud.yandex.net/translate/v2/translate")
			header(HttpHeaders.Authorization, "Bearer $iamToken")
			contentType(ContentType.Application.Json)
			this.body = body
		}
		return response.translations.first().text
	}
}

@Serializable
private data class DetectLanguageRequest(val text: String, val languageCodeHints: List<String>) {
	constructor(text: String, languageCodeHints: Collection<String>) : this(text, languageCodeHints.toList())
}

@Serializable
private data class DetectLanguageResponse(val languageCode: String)

@Serializable
private data class GetLanguagesResponse(val languages: Collection<Language>) {
	@Serializable
	data class Language(val code: String, val name: String? = null)
}

@Serializable
private data class TranslateTextRequest(
	val sourceLanguageCode: String,
	val targetLanguageCode: String,
	val texts: Collection<String>,
	val format: String = "PLAIN_TEXT"
)

@Serializable
private data class TranslateTextResponse(val translations: Collection<Translation>) {
	@Serializable
	data class Translation(val text: String)
}

fun SerializersModuleBuilder.addTranslationSerializers() {
	contextual(DetectLanguageRequest::class, DetectLanguageRequest.serializer())
	contextual(DetectLanguageResponse::class, DetectLanguageResponse.serializer())
	contextual(GetLanguagesResponse::class, GetLanguagesResponse.serializer())
	contextual(GetLanguagesResponse.Language::class, GetLanguagesResponse.Language.serializer())
	contextual(TranslateTextRequest::class, TranslateTextRequest.serializer())
	contextual(TranslateTextResponse::class, TranslateTextResponse.serializer())
	contextual(TranslateTextResponse.Translation::class, TranslateTextResponse.Translation.serializer())
}
