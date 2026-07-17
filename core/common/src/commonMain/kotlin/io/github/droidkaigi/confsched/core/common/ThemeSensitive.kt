package io.github.droidkaigi.confsched.core.common

/**
 * Marks a function whose rendering depends on the active theme. The enforcement compiler plugin
 * propagates this marker into the metadata of every caller, transitively and across modules, so
 * a `@Preview` composable that renders theme-sensitive content can be required to preview under
 * every theme.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class ThemeSensitive
