package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.ui.platform.ClipEntry

// ClipEntry has no common factory in CMP 1.12.0-alpha02; each platform builds it its own way.
internal expect fun clipEntryOfPlainText(text: String): ClipEntry
