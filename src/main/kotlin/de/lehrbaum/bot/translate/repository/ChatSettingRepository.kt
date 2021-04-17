package de.lehrbaum.bot.translate.repository

import de.lehrbaum.bot.translate.extensions.generateLogger
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface ChatSettingsRepository {
	suspend fun getSettings(chatId: Long): ChatSettings
	suspend fun activateTranslation(chatId: Long): ChatSettings
	suspend fun deactivateTranslation(chatId: Long): ChatSettings
	suspend fun addTranslationRule(chatId: Long, sourceLang: String, targetLang: String): ChatSettings
	suspend fun removeTranslationRule(chatId: Long, sourceLang: String): ChatSettings
}

private val logger = generateLogger<ChatSettingsRepositoryImpl>()

class ChatSettingsRepositoryImpl(private val settingsFile: File) : ChatSettingsRepository {

	private val fileMutex = Mutex()

	/* not so nice to use runBlocking but I'm not sure what is the correct approach. Properties cannot be suspend. */
	private val chatsSettingsCache = runBlocking { loadSettings() }

	override suspend fun getSettings(chatId: Long): ChatSettings = chatsSettingsCache.getOrElse(chatId, ::ChatSettings)

	override suspend fun activateTranslation(chatId: Long): ChatSettings {
		val chatSettings = chatsSettingsCache.getOrElse(chatId, ::ChatSettings)
		val newChatSettings = chatSettings.copy(translationActive = true)
		chatsSettingsCache[chatId] = newChatSettings
		persistSettings()
		return newChatSettings
	}

	override suspend fun deactivateTranslation(chatId: Long): ChatSettings {
		val chatSettings = chatsSettingsCache.getOrElse(chatId, ::ChatSettings)
		val newChatSettings = chatSettings.copy(translationActive = false)
		chatsSettingsCache[chatId] = newChatSettings
		persistSettings()
		return newChatSettings
	}

	override suspend fun addTranslationRule(chatId: Long, sourceLang: String, targetLang: String): ChatSettings {
		val chatSettings = chatsSettingsCache.getOrElse(chatId, ::ChatSettings)
		val newTranslationRules = chatSettings.translationRules + (sourceLang to targetLang)
		val newChatSettings = chatSettings.copy(translationRules = newTranslationRules)
		chatsSettingsCache[chatId] = newChatSettings
		persistSettings()
		return newChatSettings
	}

	override suspend fun removeTranslationRule(chatId: Long, sourceLang: String): ChatSettings {
		val chatSettings = chatsSettingsCache.getOrElse(chatId, ::ChatSettings)
		val newTranslationRules = chatSettings.translationRules - sourceLang
		val newChatSettings = chatSettings.copy(translationRules = newTranslationRules)
		chatsSettingsCache[chatId] = newChatSettings
		persistSettings()
		return newChatSettings
	}

	private suspend fun persistSettings() {
		val json = Json.encodeToString(chatsSettingsCache)
		withContext(Dispatchers.IO) {
			fileMutex.withLock {
				settingsFile.writeText(json)
			}
		}
	}

	private suspend fun loadSettings(): MutableMap<Long, ChatSettings> {
		if (!settingsFile.exists()) {
			logger.info { "Settings file did not exist." }
			return mutableMapOf()
		}
		val json = withContext(Dispatchers.IO) {
			fileMutex.withLock {
				settingsFile.readText()
			}
		}
		return Json.decodeFromString(json)
	}
}

@Serializable
data class ChatSettings(
	val translationActive: Boolean = false,
	val translationRules: Map<String, String> = mapOf()
)
