package io.github.droidkaigi.confsched.core.preview.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme

/**
 * Attach to previews with `@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)`: renders the
 * preview under one fixed colour scheme. A preview that chooses its own scheme calls
 * [KaigiPreviewTheme] directly instead.
 */
class KaigiPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        KaigiPreviewTheme(colorScheme = KaigiColorScheme.MorningMist, content = content)
    }
}
