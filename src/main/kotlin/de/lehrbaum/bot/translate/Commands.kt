package de.lehrbaum.bot.translate

import com.github.kotlintelegrambot.entities.BotCommand

object Commands {
	val ECHO: BotCommand = BotCommand("echo", "Echoes back the first parameter after the command")

	val GET_LANGUAGES: BotCommand = BotCommand("getLanguages", "")

	val START_TRANSLATING: BotCommand = BotCommand("start", "")

	val STOP_TRANSLATING: BotCommand = BotCommand("stop", "")

	val ADD_TRANSLATION_RULE: BotCommand = BotCommand("addRule", "Rule form is like en ru")

	val REMOVE_TRANSLATION_RULE: BotCommand = BotCommand("removeRule", "Argument is first part of rule like en")

	// TODO write test that ensures that every command that is passed to the bot is also in the list
}
