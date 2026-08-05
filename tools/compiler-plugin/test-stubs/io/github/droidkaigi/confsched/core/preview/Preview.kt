package io.github.droidkaigi.confsched.core.preview

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class KaigiSchemeProvider : PreviewParameterProvider<ColorScheme> {
    override val values: Sequence<ColorScheme> = emptySequence()
}

class KaigiPreviewWrapper

@Composable
fun KaigiPreviewTheme(colorScheme: ColorScheme, content: @Composable () -> Unit) {
}
