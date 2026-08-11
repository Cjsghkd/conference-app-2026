package io.github.droidkaigi.confsched.core.preview

import androidx.compose.ui.tooling.preview.Preview

/**
 * Renders one preview per locale the app translates. English is the base resource set and Japanese
 * is the translated one, so a preview of locale-sensitive content carries this instead of `@Preview`.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@Preview(name = "en", locale = "en")
@Preview(name = "ja", locale = "ja")
annotation class LocalePreviews
