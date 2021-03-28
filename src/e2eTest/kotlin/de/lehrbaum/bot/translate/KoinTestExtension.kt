package de.lehrbaum.bot.translate

import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class KoinTestExtension : BeforeEachCallback {
	override fun beforeEach(context: ExtensionContext?) {
		setupKoin()
	}
}