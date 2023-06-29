package de.lehrbaum.bot.translate.service.translation

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import de.lehrbaum.bot.translate.KoinModules
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import kotlinx.coroutines.runBlocking

class LibreTranslateTranslationServiceE2eTest : KoinTest {

	@JvmField
	@RegisterExtension
	val koinTestExtension = KoinTestExtension.create {
		modules(KoinModules.configModule, KoinModules.applicationModule, KoinModules.repositoryModule)
	}

	private val sut: LibreTranslateTranslationService by inject()

	@Test
	fun `Get languages should return a non empty list of languages`(): Unit = runBlocking {
		val languages = sut.getLanguages()
		assertThat(languages, !isEmpty and allElements(languageNotEmpty))
	}

	@Test
	fun `Detect language should detect language of simple text`(): Unit = runBlocking {
		val language = sut.detectLanguage("Just here to say hello.", listOf())
		assertThat(language, equalTo("en"))
	}

	@Test
	fun `Detect language with language suggestion should ignore the suggestion if incorrect`(): Unit = runBlocking {
		val language = sut.detectLanguage("Just here to say hello.", setOf("ar"))
		assertThat(language, equalTo("en"))
	}

	@Test
	@Disabled("Not yet implemented")
	fun `Detect language with language suggestion should consider the suggestion`(): Unit = runBlocking {
		val language = sut.detectLanguage("Blitzkrieg ist gut", setOf("en"))
		assertThat(language, equalTo("en"))
	}

	@Test
	@Disabled("Not yet implemented")
	fun `Translate language should translate simple text`(): Unit = runBlocking {
		val language = sut.translate("Hello", "en", "de")
		assertThat(language, equalTo("Hallo"))
	}
}
