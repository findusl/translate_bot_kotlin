package de.lehrbaum.bot.translate

inline fun <T> T?.exitIfNull(exitBlock: () -> Nothing): T {
	if (this == null)
		exitBlock()
	else
		return this
}