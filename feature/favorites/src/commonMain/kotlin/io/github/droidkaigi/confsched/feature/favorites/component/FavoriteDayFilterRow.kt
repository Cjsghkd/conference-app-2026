package io.github.droidkaigi.confsched.feature.favorites.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.KaigiFilterChip

@Composable
internal fun FavoriteDayFilterRow(
    selectedDayFilter: DroidKaigi2026Day?,
    onDayFilterClick: (DroidKaigi2026Day?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(FavoriteDayFilterRowDefaults.spacing),
    ) {
        KaigiFilterChip(
            selected = selectedDayFilter == null,
            onClick = { onDayFilterClick(null) },
            label = "All",
            seed = FavoriteDayFilterRowDefaults.FIRST_SEED,
        )
        DroidKaigi2026Day.entries.forEachIndexed { index, day ->
            KaigiFilterChip(
                selected = selectedDayFilter == day,
                onClick = { onDayFilterClick(day) },
                label = day.label,
                seed = FavoriteDayFilterRowDefaults.FIRST_SEED + index + 1,
            )
        }
    }
}

private object FavoriteDayFilterRowDefaults {
    val spacing = 8.dp
    const val FIRST_SEED = 821
}

@Preview
@Composable
fun FavoriteDayFilterRowPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        FavoriteDayFilterRow(selectedDayFilter = DroidKaigi2026Day.Day1, onDayFilterClick = {})
    }
}
