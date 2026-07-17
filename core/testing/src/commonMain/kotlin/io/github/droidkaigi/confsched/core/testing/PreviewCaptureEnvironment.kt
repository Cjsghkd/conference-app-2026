package io.github.droidkaigi.confsched.core.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import io.github.droidkaigi.confsched.core.preview.LocalPreviewImageResolver
import io.github.droidkaigi.confsched.core.preview.wrapper.PreviewGraphImageResolver

@Composable
fun PreviewCaptureEnvironment(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalPreviewImageResolver provides remember { PreviewGraphImageResolver() },
    ) {
        content()
    }
}
