package de.lehrbaum.bot.translate.telegram

import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.BotCommand
import de.lehrbaum.bot.translate.extensions.consumeCommand
import de.lehrbaum.bot.translate.extensions.exitIfNull
import de.lehrbaum.bot.translate.extensions.replyToMessage
import de.lehrbaum.bot.translate.repository.ChatSettings
import de.lehrbaum.bot.translate.repository.ChatSettingsRepository
import de.lehrbaum.bot.translate.service.translation.TranslationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class TranslateBotLogic(
	telegramBotFactory: TelegramBotFactory,
	private val translationService: TranslationService,
	private val chatSettingsRepository: ChatSettingsRepository
) {

	private val translateBotScope = CoroutineScope(Dispatchers.IO)

	private val bot = telegramBotFactory.generateBot {
		consumeCommand(Commands.ECHO.command) { handleEchoCommand() }
		consumeCommand(Commands.GET_LANGUAGES.command) { translateBotScope.launch { handleGetLanguagesCommand() } }
		consumeCommand(Commands.START_TRANSLATING.command) { handleStartTranslatingCommand() }
		consumeCommand(Commands.STOP_TRANSLATING.command) { handleStopTranslatingCommand() }
		consumeCommand(Commands.ADD_TRANSLATION_RULE.command) { handleAddTranslationRuleCommand() }
		consumeCommand(Commands.REMOVE_TRANSLATION_RULE.command) { handleRemoveTranslationRuleCommand() }
		consumeCommand(Commands.HELP.command) { handleHelpCommand() }
		consumeCommand(Commands.TRANSLATE.command) { translateBotScope.launch { handleTranslateCommand() } }
		text { translateBotScope.launch { handleTextMessage() } }
	}

	fun startLogic() {
		logger.info { "Start polling telegram bot" }
		bot.startPolling()
	}

	private fun CommandHandlerEnvironment.handleEchoCommand() {
		val text = args.getOrNull(0)
		replyToMessage(text ?: "Didn't get anything to echo: $message")
	}

	private suspend fun CommandHandlerEnvironment.handleGetLanguagesCommand() {
		val languagesHumanReadable = translationService.getLanguages()
			.joinToString(separator = "\n", prefix = "Possible languages are:\n") { "${it.code}: ${it.name}" }
		replyToMessage(languagesHumanReadable)
	}

	private fun CommandHandlerEnvironment.handleStartTranslatingCommand() {
		val chatSettings = chatSettingsRepository.getSettings(message.chat.id)

		if (chatSettings.translationRules.isEmpty()) {
			replyToMessage(
				"There are no active translation rules. Translating not activated. " +
						"Use ${Commands.ADD_TRANSLATION_RULE.command} to add translation rules."
			)
			return
		}

		val newSettings = chatSettingsRepository.activateTranslation(message.chat.id)
		val activeRules = newSettings.getHumanReadableTranslationRules()
		replyToMessage("Activated translations for this chat. Active rules are: $activeRules")
	}

	private fun CommandHandlerEnvironment.handleStopTranslatingCommand() {
		val chatSettings = chatSettingsRepository.getSettings(message.chat.id)
		if (chatSettings.translationActive) {
			chatSettingsRepository.deactivateTranslation(message.chat.id)
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
		val chatSettings = chatSettingsRepository.getSettings(message.chat.id)

		if (chatSettings.translationRules.containsKey(source)) {
			replyToMessage(
				"There is already a translation rule for $source to ${chatSettings.translationRules[source]}. " +
						"There can only be one rule for a given source language. " +
						"You can delete the existing rule with /${Commands.ADD_TRANSLATION_RULE.command} $source"
			)
			return
		}

		val newSettings = chatSettingsRepository.addTranslationRule(message.chat.id, source, target)
		val activeRules = newSettings.getHumanReadableTranslationRules()
		replyToMessage("Added new translation rule . Active rules are: $activeRules")
	}

	private fun CommandHandlerEnvironment.handleRemoveTranslationRuleCommand() {
		val chatSettings = chatSettingsRepository.getSettings(message.chat.id)
		if (!ensureArgumentSize(1, Commands.REMOVE_TRANSLATION_RULE)) return

		val languageCode = args[0].substringBefore("-")

		if (!chatSettings.translationRules.containsKey(languageCode)) {
			replyToMessage("There is no translation rule for language $languageCode.")
			return
		}

		val newSettings = chatSettingsRepository.removeTranslationRule(message.chat.id, languageCode)

		val activeRules = newSettings.getHumanReadableTranslationRules()
		replyToMessage("Removed translation rule for $languageCode. Active rules are: $activeRules")
	}

	private fun CommandHandlerEnvironment.handleHelpCommand() {
		val isTranslationActive = chatSettingsRepository.getSettings(message.chat.id).translationActive
		val statusHumanReadable =
			if (isTranslationActive) "Automatic translation for this chat is active. Use /stop to stop translating."
			else "Automatic translation for this chat is not active. Use /start to start translating."
		val helpText = Commands.COMMANDS.joinToString(
			separator = "\n",
			prefix = "$statusHumanReadable\nPossible commands for this bot are:\n"
		) { it.run { "/$command -> $description" } }
		replyToMessage(helpText)
	}

	private suspend fun CommandHandlerEnvironment.handleTranslateCommand() {
		val text = message.text.exitIfNull {
			replyToMessage("This command requires text after it.")
			return
		}

		val chatSettings = chatSettingsRepository.getSettings(message.chat.id)
		val suggestedLanguages = chatSettings.translationRules.keys
		val sourceLanguage = translationService.detectLanguage(text, suggestedLanguages) ?: return
		logger.debug { "Detected text \"$text\" as language $sourceLanguage" }

		val targetLanguage = chatSettings.translationRules[sourceLanguage]
		if (targetLanguage == null) {
			replyToMessage("Text was detected as $sourceLanguage, but there is no rule configured for this language.")
			return
		}

		val translation = translationService.translate(text, sourceLanguage, targetLanguage)
		replyToMessage(translation)
	}

	private suspend fun TextHandlerEnvironment.handleTextMessage() {
		val chatSettings = chatSettingsRepository.getSettings(message.chat.id)
		if (!chatSettings.translationActive) return

		val suggestedLanguages = chatSettings.translationRules.keys
		val sourceLanguage = translationService.detectLanguage(text, suggestedLanguages) ?: return
		logger.debug { "Detected text \"$text\" as language $sourceLanguage" }

		val targetLanguage = chatSettings.translationRules[sourceLanguage]
		if (targetLanguage == null) {
			logger.debug { "No rule for $sourceLanguage configured for this chat." }
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

private fun ChatSettings.getHumanReadableTranslationRules(): String =
	translationRules.map { (source, target) -> "$source-$target" }.joinToString()
