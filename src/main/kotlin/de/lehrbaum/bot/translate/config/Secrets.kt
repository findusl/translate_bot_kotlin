package de.lehrbaum.bot.translate.config

data class Secrets(val telegram: Telegram, val yandex: Yandex)

data class Telegram(val accessToken: String)

data class Yandex(val keyId: String, val serviceAccountId: String, val privateKey: String)
