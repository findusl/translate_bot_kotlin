package de.lehrbaum.bot.translate.repository

interface ChatSettingsRepository {
	fun getSettings(chatId: Long): ChatSettings
	fun activateTranslation(chatId: Long): ChatSettings
	fun deactivateTranslation(chatId: Long): ChatSettings
	fun addTranslationRule(chatId: Long, sourceLang: String, targetLang: String): ChatSettings
	fun removeTranslationRule(chatId: Long, sourceLang: String): ChatSettings
}

class ChatSettingsRepositoryImpl : ChatSettingsRepository {

	private val chatsSettingsCache: MutableMap<Long, ChatSettings> = mutableMapOf()

	override fun getSettings(chatId: Long): ChatSettings = chatsSettingsCache.getOrElse(chatId, ::ChatSettings)

	override fun activateTranslation(chatId: Long): ChatSettings {
		val chatSettings = chatsSettingsCache.getOrElse(chatId, ::ChatSettings)
		val newChatSettings = chatSettings.copy(translationActive = true)
		chatsSettingsCache[chatId] = newChatSettings
		return newChatSettings
	}

	override fun deactivateTranslation(chatId: Long): ChatSettings {
		val chatSettings = chatsSettingsCache.getOrElse(chatId, ::ChatSettings)
		val newChatSettings = chatSettings.copy(translationActive = false)
		chatsSettingsCache[chatId] = newChatSettings
		return newChatSettings
	}

	override fun addTranslationRule(chatId: Long, sourceLang: String, targetLang: String): ChatSettings {
		val chatSettings = chatsSettingsCache.getOrElse(chatId, ::ChatSettings)
		val newTranslationRules = chatSettings.translationRules + (sourceLang to targetLang)
		val newChatSettings = chatSettings.copy(translationRules = newTranslationRules)
		chatsSettingsCache[chatId] = newChatSettings
		return newChatSettings
	}

	override fun removeTranslationRule(chatId: Long, sourceLang: String): ChatSettings {
		val chatSettings = chatsSettingsCache.getOrElse(chatId, ::ChatSettings)
		val newTranslationRules = chatSettings.translationRules - sourceLang
		val newChatSettings = chatSettings.copy(translationRules = newTranslationRules)
		chatsSettingsCache[chatId] = newChatSettings
		return newChatSettings
	}
}

data class ChatSettings(
	val translationActive: Boolean = false,
	val translationRules: Map<String, String> = mapOf()
)
