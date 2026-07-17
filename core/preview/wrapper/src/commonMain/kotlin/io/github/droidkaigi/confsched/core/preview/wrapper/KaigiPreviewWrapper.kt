package io.github.droidkaigi.confsched.core.preview.wrapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import dev.zacsweers.metro.createGraph
import io.github.droidkaigi.confsched.core.designsystem.KaigiTheme
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.LocalPreviewImageResolver

/**
 * Attach to previews with `@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)`: applies
 * KaigiTheme and supplies the preview image resolver through `LocalPreviewImageResolver`. The
 * resolver graph is created lazily so production code paths never construct it; its binding lives
 * in `:core:preview:impl`, a compileOnly dependency here, so that module must be present on the
 * classpath that renders previews (the IDE preview classpath, the screenshot-test runtime).
 */
class KaigiPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        val resolver = remember { createGraph<PreviewGraph>().previewImageResolver }
        KaigiTheme(colorScheme = KaigiColorScheme.MorningMist) {
            CompositionLocalProvider(LocalPreviewImageResolver provides resolver) {
                content()
            }
        }
    }
}
