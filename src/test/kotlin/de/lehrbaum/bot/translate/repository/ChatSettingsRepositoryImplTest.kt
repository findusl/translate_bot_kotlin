package de.lehrbaum.bot.translate.repository

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import de.lehrbaum.bot.translate.mapContainsEntry
import de.lehrbaum.bot.translate.mapIsEmpty
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ChatSettingsRepositoryImplTest {

	@Test
	fun `getSettings returns empty settings if file does not exist`(@TempDir tempDir: File) = runBlocking {
		val settingsFile = File(tempDir, "settings.json")
		assertFalse(settingsFile.exists())
		val repository = ChatSettingsRepositoryImpl(settingsFile)

		val settings = repository.getSettings(0)

		assertThat(settings, translationIsInactive and translationRulesAreEmpty)
	}

	@Test
	fun `getSettings returns empty settings if file is empty`(@TempDir tempDir: File) = runBlocking {
		val settingsFile = File(tempDir, "settings.json")
		settingsFile.writeText("{}")
		assertTrue(settingsFile.exists())
		val repository = ChatSettingsRepositoryImpl(settingsFile)

		val settings = repository.getSettings(0)

		assertThat(settings, translationIsInactive and translationRulesAreEmpty)
	}

	@Test
	fun `activateTranslationRule persists to settings file`(@TempDir tempDir: File) = runBlocking {
		val settingsFile = File(tempDir, "settings.json")
		assertFalse(settingsFile.exists())
		val repository = ChatSettingsRepositoryImpl(settingsFile)

		repository.activateTranslation(0)

		val json = settingsFile.readText()
		assertThat(json, containsSubstring("translationActive"))
	}

	@Test
	fun `addTranslationRule persists to settings file`(@TempDir tempDir: File) = runBlocking {
		val settingsFile = File(tempDir, "settings.json")
		settingsFile.writeText("{}")
		assertTrue(settingsFile.exists())
		val repository = ChatSettingsRepositoryImpl(settingsFile)

		repository.addTranslationRule(0, "en", "de")

		val json = settingsFile.readText()
		assertThat(json, containsSubstring("en") and containsSubstring("de"))
	}

	@Test
	fun `repository saves settings`(@TempDir tempDir: File) = runBlocking {
		val settingsFile = File(tempDir, "settings.json")
		assertFalse(settingsFile.exists())
		val repository = ChatSettingsRepositoryImpl(settingsFile)

		repository.activateTranslation(0)
		repository.addTranslationRule(0, "en", "de")
		repository.addTranslationRule(1, "ru", "ch")

		val chatSettings0 = repository.getSettings(0)
		assertThat(chatSettings0, translationIsActive and hasTranslationRule("en", "de"))
		val chatSettings1 = repository.getSettings(1)
		assertThat(chatSettings1, translationIsInactive and hasTranslationRule("ru", "ch"))
	}

	@Test
	fun `repository can read it's saved settings`(@TempDir tempDir: File) = runBlocking {
		val settingsFile = File(tempDir, "settings.json")
		assertFalse(settingsFile.exists())
		val setupRepository = ChatSettingsRepositoryImpl(settingsFile)

		setupRepository.activateTranslation(0)
		setupRepository.activateTranslation(1)
		setupRepository.addTranslationRule(1, "en", "de")
		setupRepository.addTranslationRule(2, "ru", "ch")
		setupRepository.deactivateTranslation(3)

		val verifyRepository = ChatSettingsRepositoryImpl(settingsFile)

		verifyRepository.assertChatSettings(0, translationIsActive)
		verifyRepository.assertChatSettings(
			1, translationIsActive and hasTranslationRule("en", "de")
		)
		verifyRepository.assertChatSettings(
			2, translationIsInactive and hasTranslationRule("ru", "ch")
		)
		verifyRepository.assertChatSettings(3, translationIsInactive)
	}
}

private val translationIsInactive = has(ChatSettings::translationActive, equalTo(false))
private val translationIsActive = has(ChatSettings::translationActive, equalTo(true))

private val translationRulesAreEmpty = has(ChatSettings::translationRules, mapIsEmpty)
private fun hasTranslationRule(sourceLang: String, targetLang: String): Matcher<ChatSettings> =
	has(ChatSettings::translationRules, mapContainsEntry(sourceLang, targetLang))

private suspend fun ChatSettingsRepository.assertChatSettings(chatId: Long, matcher: Matcher<ChatSettings>) {
	assertThat(getSettings(chatId), matcher) { "Failed for chat with id $chatId" }
}
