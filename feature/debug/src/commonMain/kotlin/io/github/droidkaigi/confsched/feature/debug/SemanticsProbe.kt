package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.Composable

// Registers the platform's Compose roots with the Compose Semantics Inspector. JetWhale ships a
// probe for Android and desktop only; the other targets report an empty node tree.
@Composable
internal expect fun SemanticsProbe()
