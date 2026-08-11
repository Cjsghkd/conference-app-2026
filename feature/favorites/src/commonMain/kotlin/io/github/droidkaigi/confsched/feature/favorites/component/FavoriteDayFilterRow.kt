package io.github.droidkaigi.confsched.feature.favorites.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme

@Composable
internal fun FavoriteDayFilterRow(
    selectedDayFilter: DroidKaigi2026Day?,
    onDayFilterClick: (DroidKaigi2026Day?) -> Unit,
) {
    val optionCount = DroidKaigi2026Day.entries.size + 1
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = selectedDayFilter == null,
                onClick = { onDayFilterClick(null) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = optionCount),
                modifier = Modifier.width(FavoriteDayFilterRowDefaults.tabWidth),
            ) {
                Text("All")
            }
            DroidKaigi2026Day.entries.forEachIndexed { index, day ->
                SegmentedButton(
                    selected = selectedDayFilter == day,
                    onClick = { onDayFilterClick(day) },
                    shape = SegmentedButtonDefaults.itemShape(index = index + 1, count = optionCount),
                    modifier = Modifier.width(FavoriteDayFilterRowDefaults.tabWidth),
                ) {
                    Text(day.name)
                }
            }
        }
    }
}

private object FavoriteDayFilterRowDefaults {
    val tabWidth = 104.dp
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
