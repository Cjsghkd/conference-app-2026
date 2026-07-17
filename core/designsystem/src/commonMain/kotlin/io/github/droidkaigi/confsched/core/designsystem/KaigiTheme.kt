package io.github.droidkaigi.confsched.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme

// Provisional palettes derived from the Figma theme candidates (node 40:2540).
// Each Figma theme defines only a foreground/background pair; container and
// surface tones are interpolated between them and await designer review.
// The call-to-action orange is shared across themes as tertiary.
private val CtaOrange = Color(0xFFE04A1E)

private val MorningMist = lightColorScheme(
    primary = Color(0xFF2A3A52),
    onPrimary = Color(0xFFD6DCE8),
    primaryContainer = Color(0xFFB8C3D6),
    onPrimaryContainer = Color(0xFF1A2638),
    secondary = Color(0xFF51617A),
    onSecondary = Color(0xFFE2E6EF),
    tertiary = CtaOrange,
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFD6DCE8),
    onBackground = Color(0xFF2A3A52),
    surface = Color(0xFFE2E6EF),
    onSurface = Color(0xFF2A3A52),
    surfaceVariant = Color(0xFFCAD2E1),
    onSurfaceVariant = Color(0xFF3D4D66),
    outline = Color(0xFF6B7A94),
)

private val DeepTeal = darkColorScheme(
    primary = Color(0xFFE8C97A),
    onPrimary = Color(0xFF1A3D45),
    primaryContainer = Color(0xFF2E555E),
    onPrimaryContainer = Color(0xFFF2DFA8),
    secondary = Color(0xFFB8CDBF),
    onSecondary = Color(0xFF1A3D45),
    tertiary = CtaOrange,
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF1A3D45),
    onBackground = Color(0xFFE8C97A),
    surface = Color(0xFF214750),
    onSurface = Color(0xFFE8C97A),
    surfaceVariant = Color(0xFF2A505A),
    onSurfaceVariant = Color(0xFFD8C08F),
    outline = Color(0xFF7A9299),
)

private val SakuraPlum = lightColorScheme(
    primary = Color(0xFF5C2E2A),
    onPrimary = Color(0xFFF5D5D0),
    primaryContainer = Color(0xFFE3B3AC),
    onPrimaryContainer = Color(0xFF421F1C),
    secondary = Color(0xFF7A4A44),
    onSecondary = Color(0xFFF8E2DE),
    tertiary = CtaOrange,
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF5D5D0),
    onBackground = Color(0xFF5C2E2A),
    surface = Color(0xFFF8E2DE),
    onSurface = Color(0xFF5C2E2A),
    surfaceVariant = Color(0xFFEDC6BF),
    onSurfaceVariant = Color(0xFF6E3B36),
    outline = Color(0xFFA37671),
)

private val Terracotta = lightColorScheme(
    primary = Color(0xFF3D2418),
    onPrimary = Color(0xFFE8B98A),
    primaryContainer = Color(0xFFC98F5C),
    onPrimaryContainer = Color(0xFF2A180F),
    secondary = Color(0xFF6B4630),
    onSecondary = Color(0xFFF0D0AC),
    tertiary = CtaOrange,
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFE8B98A),
    onBackground = Color(0xFF3D2418),
    surface = Color(0xFFF0CCA4),
    onSurface = Color(0xFF3D2418),
    surfaceVariant = Color(0xFFDEAB79),
    onSurfaceVariant = Color(0xFF503222),
    outline = Color(0xFF8F6A4C),
)

private val CampfireNight = darkColorScheme(
    primary = Color(0xFFE04A1E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF5A2E1C),
    onPrimaryContainer = Color(0xFFF2A88C),
    secondary = Color(0xFFC9A98E),
    onSecondary = Color(0xFF2D2620),
    tertiary = CtaOrange,
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF2D2620),
    onBackground = Color(0xFFEADFD5),
    surface = Color(0xFF362E27),
    onSurface = Color(0xFFEADFD5),
    surfaceVariant = Color(0xFF423830),
    onSurfaceVariant = Color(0xFFCFBBA9),
    outline = Color(0xFF8C7A6A),
)

private fun KaigiColorScheme.toMaterialColorScheme() = when (this) {
    KaigiColorScheme.MorningMist -> MorningMist
    KaigiColorScheme.DeepTeal -> DeepTeal
    KaigiColorScheme.SakuraPlum -> SakuraPlum
    KaigiColorScheme.Terracotta -> Terracotta
    KaigiColorScheme.CampfireNight -> CampfireNight
}

@Composable
fun KaigiTheme(
    colorScheme: KaigiColorScheme,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme.toMaterialColorScheme(),
        content = content,
    )
}
