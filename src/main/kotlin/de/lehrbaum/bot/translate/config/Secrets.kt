package de.lehrbaum.bot.translate.config

import kotlinx.serialization.Serializable

@Serializable
data class Secrets(val telegram: Telegram, val yandex: Yandex)

@Serializable
data class Telegram(val accessToken: String)

@Serializable
data class Yandex(val keyId: String, val serviceAccountId: String, val privateKey: String)
