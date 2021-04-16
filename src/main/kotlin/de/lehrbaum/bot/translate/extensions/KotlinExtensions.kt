package de.lehrbaum.bot.translate.extensions

import java.util.logging.Logger

inline fun <T> T?.exitIfNull(exitBlock: () -> Nothing): T {
	if (this == null)
		exitBlock()
	else
		return this
}

inline fun <reified T : Any> generateLogger(): Logger {
	return Logger.getLogger(T::class.qualifiedName)
}
