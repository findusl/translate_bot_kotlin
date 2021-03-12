package de.lehrbaum.bot.translate

import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.BotCommand

private val possibleLanguages: List<String> = listOf("en", "de", "ru")

class TranslateBotLogic(
	telegramBotFactory: TelegramBotFactory,
	translationService: FakeTranslationService
) {

	private val chatsSettings: MutableMap<Long, ChatSettings> = mutableMapOf()

	private val bot = telegramBotFactory.generateBot {
		command(Commands.ECHO.command) { handleEchoCommand() }
		command(Commands.GET_LANGUAGES.command) { handleGetLanguagesCommand() }
		command(Commands.START_TRANSLATING.command) { handleStartTranslatingCommand() }
		command(Commands.STOP_TRANSLATING.command) { handleStopTranslatingCommand() }
		command(Commands.ADD_TRANSLATION_RULE.command) { handleAddTranslationRuleCommand() }
		command(Commands.REMOVE_TRANSLATION_RULE.command) { handleRemoveTranslationRuleCommand() }
		text {

		}
	}

	fun startLogic() {
		println("Start polling telegram bot")
		bot.startPolling()
	}

	/*
	 * Args contains the words after the command separated by spaces
	 */

	private fun CommandHandlerEnvironment.handleEchoCommand() {
		val text = args.getOrNull(0)
		replyToMessage(text ?: "Didn't get anything to echo: $message")
	}

	private fun CommandHandlerEnvironment.handleGetLanguagesCommand() {
		replyToMessage("Possible languages are: " + possibleLanguages.joinToString())
	}

	private fun CommandHandlerEnvironment.handleStartTranslatingCommand() {
		val chatSettings = chatsSettings.getOrDefault(message.chat.id, ChatSettings())

		if (chatSettings.translationRules.isEmpty()) {
			replyToMessage(
				"There are no active translation rules. Translating not activated. " +
					"Use ${Commands.ADD_TRANSLATION_RULE.command} to add translation rules."
			)
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
		val chatSettings = chatsSettings.getOrPut(message.chat.id, ::ChatSettings)
		if (!ensureArgumentSize(1, Commands.ADD_TRANSLATION_RULE)) return

		val (source, target) = args[0].tryExtractLanguagePair().exitIfNull {
			replyToMessage("Incorrect parameter.\n" + Commands.ADD_TRANSLATION_RULE.description)
			return
		}

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

	private fun CommandHandlerEnvironment.handleTextMessage() {
		val chatSettings = chatsSettings[message.chat.id]
		if (chatSettings?.translationActive != true) return


	}

	private fun CommandHandlerEnvironment.ensureArgumentSize(expectedSize: Int, command: BotCommand): Boolean {
		if (args.size != expectedSize) {
			replyToMessage("Incorrect count of parameters.\n" + command.description)
			return false
		}
		return true
	}
}

private fun String.tryExtractLanguagePair(): Pair<String, String>? {
	val parts = split("-")
	if (parts.size != 2 || parts.any { it.length != 2 }) {
		return null
	}

	// TODO ensure valid language codes

	return Pair(parts[0], parts[1])
}

private data class ChatSettings(
	var translationActive: Boolean = false,
	var translationRules: MutableMap<String, String> = mutableMapOf()
)

private fun ChatSettings.getHumanReadableTranslationRules(): String =
	translationRules.map { (source, target) -> "$source-$target" }.joinToString()
