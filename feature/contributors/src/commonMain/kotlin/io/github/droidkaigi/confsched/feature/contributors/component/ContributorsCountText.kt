package io.github.droidkaigi.confsched.feature.contributors.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.LocalePreviews
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.feature.contributors.generated.resources.Res
import io.github.droidkaigi.confsched.feature.contributors.generated.resources.contributors_count
import org.jetbrains.compose.resources.pluralStringResource

@Composable
internal fun ContributorsCountText(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = pluralStringResource(Res.plurals.contributors_count, count, count),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@LocalePreviews
@Composable
fun ContributorsCountTextPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        ContributorsCountText(count = 42)
    }
}
