package io.github.droidkaigi.confsched.core.preview.wrapper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider

/**
 * Every color the active scheme defines, each swatch labeled in the color the scheme pairs with it,
 * so a change to the palettes can be reviewed for both value and legibility without running the app.
 * Tokens that carry no paired on-color are labeled in `onSurface` and drawn beside a `surface`
 * reference swatch, which is what makes an unreadable pairing visible.
 */
@Composable
fun ColorSchemeCatalog(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .background(scheme.surface)
            .padding(vertical = 8.dp),
    ) {
        for ((label, fill, onFill) in scheme.swatches()) {
            Swatch(label = label, fill = fill, onFill = onFill)
        }
    }
}

@Composable
private fun Swatch(
    label: String,
    fill: Color,
    onFill: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(160.dp),
        )
        Text(
            text = label,
            color = onFill,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .background(fill)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

private fun ColorScheme.swatches(): List<Triple<String, Color, Color>> = listOf(
    Triple("primary", primary, onPrimary),
    Triple("primaryContainer", primaryContainer, onPrimaryContainer),
    Triple("secondary", secondary, onSecondary),
    Triple("secondaryContainer", secondaryContainer, onSecondaryContainer),
    Triple("tertiary", tertiary, onTertiary),
    Triple("tertiaryContainer", tertiaryContainer, onTertiaryContainer),
    Triple("error", error, onError),
    Triple("errorContainer", errorContainer, onErrorContainer),
    Triple("background", background, onBackground),
    Triple("surface", surface, onSurface),
    Triple("surfaceVariant", surfaceVariant, onSurfaceVariant),
    Triple("surfaceBright", surfaceBright, onSurface),
    Triple("surfaceDim", surfaceDim, onSurface),
    Triple("surfaceContainerLowest", surfaceContainerLowest, onSurface),
    Triple("surfaceContainerLow", surfaceContainerLow, onSurface),
    Triple("surfaceContainer", surfaceContainer, onSurface),
    Triple("surfaceContainerHigh", surfaceContainerHigh, onSurface),
    Triple("surfaceContainerHighest", surfaceContainerHighest, onSurface),
    Triple("inverseSurface", inverseSurface, inverseOnSurface),
    Triple("inversePrimary", inversePrimary, inverseSurface),
    Triple("outline", outline, surface),
    Triple("outlineVariant", outlineVariant, onSurface),
    Triple("scrim", scrim, surface),
)

@Preview
@Composable
private fun ColorSchemeCatalogPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        ColorSchemeCatalog()
    }
}
