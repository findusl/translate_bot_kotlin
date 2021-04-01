package de.lehrbaum.bot.translate.telegram

import com.github.kotlintelegrambot.entities.BotCommand

object Commands {
	val ECHO: BotCommand = BotCommand("echo", "Echoes back the first word after the command")

	val GET_LANGUAGES: BotCommand = BotCommand(
		"getLanguages",
		"Retrieve all languages. The languages have the format 'code':'name'. " +
				"For further commands the 'code' part is used."
	)

	val START_TRANSLATING: BotCommand =
		BotCommand("start", "Start translating any messages sent to this chat.")

	val STOP_TRANSLATING: BotCommand =
		BotCommand("stop", "Stop translating any messages sent to this chat.")

	val ADD_TRANSLATION_RULE: BotCommand = BotCommand(
		"addRule",
		"Rules consist of the language code of the source language and target language. e.g. en ru"
	)

	val REMOVE_TRANSLATION_RULE: BotCommand =
		BotCommand("removeRule", "Argument is first part of rule, e.g. en")

	val HELP: BotCommand by lazy { BotCommand("help", "Lists all possible commands") }

	/** Contains all commands listed in this class. */
	val COMMANDS: List<BotCommand> = listOf(
		ECHO,
		GET_LANGUAGES,
		START_TRANSLATING,
		STOP_TRANSLATING,
		ADD_TRANSLATION_RULE,
		REMOVE_TRANSLATION_RULE,
		HELP
	)

	// TODO write test that ensures that every command that is passed to the bot is also in the list
}
