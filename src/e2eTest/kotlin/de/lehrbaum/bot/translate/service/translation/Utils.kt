package de.lehrbaum.bot.translate.service.translation

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.isBlank


val languageNotEmpty: Matcher<Language> = has(Language::code, !isBlank) and has(Language::name, !isBlank)
