package io.github.droidkaigi.confsched.core.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme

class KaigiSchemeProvider : PreviewParameterProvider<KaigiColorScheme> {
    override val values: Sequence<KaigiColorScheme> = KaigiColorScheme.entries.asSequence()
}
