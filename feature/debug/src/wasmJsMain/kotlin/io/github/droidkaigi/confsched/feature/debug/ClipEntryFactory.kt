package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.ui.platform.ClipEntry

internal actual fun clipEntryOfPlainText(text: String): ClipEntry =
    ClipEntry.withPlainText(text)
