package io.github.droidkaigi.confsched.feature.debug

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

internal actual fun clipEntryOfPlainText(text: String): ClipEntry =
    ClipEntry(ClipData.newPlainText("text", text))
