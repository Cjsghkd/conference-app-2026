package io.github.droidkaigi.confsched.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import io.github.droidkaigi.confsched.core.model.MultiLangText

private const val JAPANESE = "ja"

/**
 * The text in the language the app is running in. Compose Resources resolves its own strings
 * against the same [Locale], so server text and string resources always agree on the language.
 */
@Composable
fun MultiLangText.current(): String = if (Locale.current.language == JAPANESE) ja else en
