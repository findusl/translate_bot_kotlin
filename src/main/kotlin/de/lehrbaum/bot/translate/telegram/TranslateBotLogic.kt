package de.lehrbaum.bot.translate.telegram

import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.BotCommand
import de.lehrbaum.bot.translate.Commands
import de.lehrbaum.bot.translate.consumeCommand
import de.lehrbaum.bot.translate.exitIfNull
import de.lehrbaum.bot.translate.replyToMessage
import de.lehrbaum.bot.translate.service.translation.TranslationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TranslateBotLogic(
	telegramBotFactory: TelegramBotFactory,
	private val translationService: TranslationService
) {

	private val chatsSettings: MutableMap<Long, ChatSettings> = mutableMapOf()

	private val translateBotScope = CoroutineScope(Dispatchers.IO)

	private val bot = telegramBotFactory.generateBot {
		consumeCommand(Commands.ECHO.command) { handleEchoCommand() }
		consumeCommand(Commands.GET_LANGUAGES.command) { translateBotScope.launch { handleGetLanguagesCommand() } }
		consumeCommand(Commands.START_TRANSLATING.command) { handleStartTranslatingCommand() }
		consumeCommand(Commands.STOP_TRANSLATING.command) { handleStopTranslatingCommand() }
		consumeCommand(Commands.ADD_TRANSLATION_RULE.command) { handleAddTranslationRuleCommand() }
		consumeCommand(Commands.REMOVE_TRANSLATION_RULE.command) { handleRemoveTranslationRuleCommand() }
		text { translateBotScope.launch { handleTextMessage() } }
	}

	fun startLogic() {
		println("Start polling telegram bot")
		bot.startPolling()
	}

	private fun CommandHandlerEnvironment.handleEchoCommand() {
		val text = args.getOrNull(0)
		replyToMessage(text ?: "Didn't get anything to echo: $message")
	}

	private suspend fun CommandHandlerEnvironment.handleGetLanguagesCommand() {
		val languagesHumanReadable = translationService.getLanguages()
			.joinToString { "${it.code}: ${it.name},\n" }
		replyToMessage("Possible languages are: $languagesHumanReadable")
	}

	private fun CommandHandlerEnvironment.handleStartTranslatingCommand() {
		val chatSettings = chatsSettings.getOrDefault(message.chat.id, ChatSettings())

		if (chatSettings.translationRules.isEmpty()) {
			replyToMessage(
				"There are no active translation rules. Translating not activated. " +
					"Use ${Commands.ADD_TRANSLATION_RULE.command} to add translation rules."
			)
			return
		}

		chatSettings.translationActive = true
		val activeRules = chatSettings.getHumanReadableTranslationRules()
		replyToMessage("Activated translations for this chat. Active rules are: $activeRules")
	}

	private fun CommandHandlerEnvironment.handleStopTranslatingCommand() {
		val chatSettings = chatsSettings.getOrPut(message.chat.id, ::ChatSettings)
		if (chatSettings.translationActive) {
			chatSettings.translationActive = false
			replyToMessage("No more messages in this chat are automatically translated.")
		} else {
			replyToMessage(
				"No more messages in this chat are automatically translated. " +
					"Translation for this chat was never active."
			)
		}
	}

	private fun CommandHandlerEnvironment.handleAddTranslationRuleCommand() {
		val (source, target) = when (args.size) {
			1 -> args[0].tryParseLanguagePair().exitIfNull {
				replyToMessage("Incorrect parameter.\n" + Commands.ADD_TRANSLATION_RULE.description)
				return
			}
			2 -> Pair(args[0], args[1])
			else -> {
				replyToMessage("Incorrect count of parameters.\n" + Commands.ADD_TRANSLATION_RULE.description)
				return
			}
		}

		addTranslationRule(source, target)
	}

	private fun CommandHandlerEnvironment.addTranslationRule(source: String, target: String) {
		val chatSettings = chatsSettings.getOrPut(message.chat.id, ::ChatSettings)

		if (chatSettings.translationRules.containsKey(source)) {
			replyToMessage(
				"There is already a translation rule for $source to ${chatSettings.translationRules[source]}. " +
					"There can only be one rule for a given source language. " +
					"You can delete the existing rule with /${Commands.ADD_TRANSLATION_RULE.command} $source"
			)
			return
		}

		chatSettings.translationRules[source] = target

		val activeRules = chatSettings.getHumanReadableTranslationRules()
		replyToMessage("Added new translation rule . Active rules are: $activeRules")
	}

	private fun CommandHandlerEnvironment.handleRemoveTranslationRuleCommand() {
		val chatSettings = chatsSettings.getOrPut(message.chat.id, ::ChatSettings)
		if (!ensureArgumentSize(1, Commands.REMOVE_TRANSLATION_RULE)) return

		val languageCode = args[0].substringBefore("-")

		if (!chatSettings.translationRules.containsKey(languageCode)) {
			replyToMessage("There is no translation rule for language $languageCode.")
			return
		}

		chatSettings.translationRules.remove(languageCode)

		val activeRules = chatSettings.getHumanReadableTranslationRules()
		replyToMessage("Removed translation rule for $languageCode. Active rules are: $activeRules")
	}

	private suspend fun TextHandlerEnvironment.handleTextMessage() {
		val chatSettings = chatsSettings[message.chat.id]
		if (chatSettings?.translationActive != true) return

		val suggestedLanguages = chatSettings.translationRules.keys
		val sourceLanguage = translationService.detectLanguage(text, suggestedLanguages) ?: return
		println("Detected text \"$text\" as language $sourceLanguage")

		val targetLanguage = chatSettings.translationRules[sourceLanguage]
		if (targetLanguage == null) {
			println("No rule for $sourceLanguage detected for text $text")
			return
		}

		val translation = translationService.translate(text, sourceLanguage, targetLanguage)
		replyToMessage(translation)
	}

	private fun CommandHandlerEnvironment.ensureArgumentSize(expectedSize: Int, command: BotCommand): Boolean {
		if (args.size != expectedSize) {
			replyToMessage("Incorrect count of parameters.\n" + command.description)
			return false
		}
		return true
	}
}

private fun String.tryParseLanguagePair(): Pair<String, String>? {
	val parts = split("-")
	if (parts.size != 2 || parts.any { it.length != 2 }) {
		return null
	}

	return Pair(parts[0], parts[1])
}

private data class ChatSettings(
	var translationActive: Boolean = false,
	var translationRules: MutableMap<String, String> = mutableMapOf()
)

private fun ChatSettings.getHumanReadableTranslationRules(): String =
	translationRules.map { (source, target) -> "$source-$target" }.joinToString()
