package io.github.droidkaigi.confsched.core.common

/**
 * Marks a function whose rendering depends on the active locale. The enforcement compiler plugin
 * propagates this marker into the metadata of every caller, transitively and across modules, so
 * a `@Preview` composable that renders locale-sensitive content can be required to preview under
 * every locale the app translates.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class LocaleSensitive
