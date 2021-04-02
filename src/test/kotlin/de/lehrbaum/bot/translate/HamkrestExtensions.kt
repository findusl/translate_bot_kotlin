package de.lehrbaum.bot.translate

import com.natpryce.hamkrest.*
import org.hamcrest.CoreMatchers

val mapIsEmpty: Matcher<Map<*, *>> = has(Map<*, *>::size, equalTo(0))

fun <K, V, M : Map<K, V>> mapContainsEntry(key: K, value: V): Matcher<M> =
	has(Map<K, V>::entries, anyElement(mapEntryMatches(key, value)))

private fun <K, V> mapEntryMatches(key: K, value: V) =
	has(Map.Entry<K, V>::key, equalTo(key)) and has(Map.Entry<K, V>::value, equalTo(value))


