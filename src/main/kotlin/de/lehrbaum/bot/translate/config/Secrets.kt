package de.lehrbaum.bot.translate.config

import kotlinx.serialization.Serializable

@Serializable
data class Secrets(val telegram: Telegram, val yandex: Yandex, val deepl: Deepl, val libreTranslate: LibreTranslate)

@Serializable
data class Telegram(val accessToken: String)

@Serializable
data class Yandex(val keyId: String, val serviceAccountId: String, val privateKey: String)

@Serializable
data class Deepl(val apiKey: String)

@Serializable
data class LibreTranslate(val baseUrl: String)
