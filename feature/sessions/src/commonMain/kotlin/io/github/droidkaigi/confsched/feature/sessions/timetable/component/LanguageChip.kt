package io.github.droidkaigi.confsched.feature.sessions.timetable.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.model.Language
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.KaigiChip
import io.github.droidkaigi.confsched.core.ui.KaigiChipDefaults

/** The language a session is delivered in. */
@Composable
internal fun LanguageChip(language: Language, seed: Int) {
    KaigiChip(
        seed = seed,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(text = language.label, style = KaigiChipDefaults.labelStyle)
    }
}

@Preview
@Composable
fun LanguageChipPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        LanguageChip(language = Language.ENGLISH, seed = 2)
    }
}
