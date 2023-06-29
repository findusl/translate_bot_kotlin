package de.lehrbaum.bot.translate.service.translation

import com.natpryce.hamkrest.allElements
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmpty
import de.lehrbaum.bot.translate.KoinModules
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import kotlinx.coroutines.runBlocking

class DeeplTranslationServiceE2eTest : KoinTest {

	@JvmField
	@RegisterExtension
	val koinTestExtension = KoinTestExtension.create {
		modules(KoinModules.configModule, KoinModules.applicationModule, KoinModules.repositoryModule)
	}

	private val sut: DeeplTranslationService by inject()

	@Test
	@Disabled("Not yet implemented")
	fun `Get languages should return a non empty list of languages`(): Unit = runBlocking {
		val languages = sut.getLanguages()
		assertThat(languages, !isEmpty and allElements(languageNotEmpty))
	}

	@Test
	@Disabled("Not yet implemented")
	fun `Detect language should detect language of simple text`(): Unit = runBlocking {
		val language = sut.detectLanguage("Just here to say hello.", listOf())
		assertThat(language, equalTo("en"))
	}

	@Test
	@Disabled("Not yet implemented")
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
	fun `Translate language should translate simple text`(): Unit = runBlocking {
		val language = sut.translate("Hello", "en", "de")
		assertThat(language, equalTo("Hallo"))
	}
}