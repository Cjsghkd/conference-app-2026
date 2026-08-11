package io.github.droidkaigi.confsched.core.model

/**
 * Text the conference API supplies in both of its languages. The language is chosen where the text
 * is displayed rather than where it is fetched, so a cached response outlives a locale change.
 */
data class MultiLangText(val ja: String, val en: String)
