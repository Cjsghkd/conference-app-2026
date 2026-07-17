package io.github.droidkaigi.confsched.core.preview

import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class KaigiSchemeProvider : PreviewParameterProvider<KaigiColorScheme> {
    override val values: Sequence<KaigiColorScheme> = KaigiColorScheme.entries.asSequence()
}
