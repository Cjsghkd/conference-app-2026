package io.github.droidkaigi.confsched.core.preview

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.designsystem.KaigiTheme
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme

@Composable
fun MultiThemedPreviewTheme(
    colorScheme: KaigiColorScheme,
    content: @Composable () -> Unit,
) {
    KaigiTheme(colorScheme = colorScheme, content = content)
}
