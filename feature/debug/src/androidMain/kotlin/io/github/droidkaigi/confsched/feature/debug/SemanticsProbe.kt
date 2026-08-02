package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.Composable
import com.kitakkun.jetwhale.plugins.semantics.agent.JetWhaleSemanticsProbe

@Composable
internal actual fun SemanticsProbe() {
    JetWhaleSemanticsProbe()
}
