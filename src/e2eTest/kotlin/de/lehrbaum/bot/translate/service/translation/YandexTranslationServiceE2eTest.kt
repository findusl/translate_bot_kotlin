package de.lehrbaum.bot.translate.service.translation

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import de.lehrbaum.bot.translate.KoinTestExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.get

/**
 * This test does something I learned not to do. It tests the 3rd party consumed service for correctness.
 * I anyway wrote the test because I want to easily react to any problems with the API of the consumed service.
 */
class YandexTranslationServiceE2eTest : KoinTest {

	@JvmField
	@RegisterExtension
	val koinTestExtension = KoinTestExtension()

	@Test
	fun `Get languages should return a non empty list of languages`(): Unit = runBlocking {
		val yandexTranslationService = YandexTranslationService(get(), get())
		val languages = yandexTranslationService.getLanguages()
		assertThat(languages, !isEmpty and allElements(languageNotEmpty))
	}

	@Test
	fun `Detect language should detect language of simple text`(): Unit = runBlocking {
		val yandexTranslationService = YandexTranslationService(get(), get())
		val language = yandexTranslationService.detectLanguage("Just here to say hello.", listOf())
		assertThat(language, equalTo("en"))
	}

	@Test
	fun `Detect language with language suggestion should ignore the suggestion if incorrect`(): Unit = runBlocking {
		val yandexTranslationService = YandexTranslationService(get(), get())
		val language = yandexTranslationService.detectLanguage("Just here to say hello.", setOf("ar"))
		assertThat(language, equalTo("en"))
	}

	@Test
	fun `Detect language with language suggestion should consider the suggestion`(): Unit = runBlocking {
		val yandexTranslationService = YandexTranslationService(get(), get())
		val language = yandexTranslationService.detectLanguage("Blitzkrieg", setOf("en"))
		assertThat(language, equalTo("en"))
	}

	@Test
	fun `Translate language should translate simple text`(): Unit = runBlocking {
		val yandexTranslationService = YandexTranslationService(get(), get())
		val language = yandexTranslationService.translate("Hello", "en", "de")
		assertThat(language, equalTo("Hallo"))
	}
}

private val languageNotEmpty: Matcher<Language> = has(Language::code, !isBlank) and has(Language::name, !isBlank)
