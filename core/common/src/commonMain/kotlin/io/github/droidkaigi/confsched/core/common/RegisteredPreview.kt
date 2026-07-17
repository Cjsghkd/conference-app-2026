package io.github.droidkaigi.confsched.core.common

import androidx.compose.runtime.Composable

/**
 * A preview entry in the KSP-generated per-module PreviewRegistry. Non-JVM screenshot tests
 * (iOS) cannot discover previews by classpath scanning, so the registry enumerates them at
 * compile time; the desktop test shares the same path for consistency.
 */
class RegisteredPreview(
    val name: String,
    val content: @Composable () -> Unit,
)
