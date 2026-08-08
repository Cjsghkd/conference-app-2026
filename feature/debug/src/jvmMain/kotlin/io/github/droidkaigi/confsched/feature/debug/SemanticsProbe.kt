package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.awt.LocalAwtWindow
import androidx.compose.ui.window.FrameWindowScope
import com.kitakkun.jetwhale.plugins.semantics.agent.JetWhaleSemanticsProbe

// The desktop probe is scoped to a window and declared on FrameWindowScope, which KaigiApp is not
// called with; LocalAwtWindow carries the same ComposeWindow down the composition.
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun SemanticsProbe() {
    val window = LocalAwtWindow.current as? ComposeWindow ?: return
    val windowScope = remember(window) { ProbeWindowScope(window) }
    windowScope.JetWhaleSemanticsProbe()
}

private class ProbeWindowScope(override val window: ComposeWindow) : FrameWindowScope
