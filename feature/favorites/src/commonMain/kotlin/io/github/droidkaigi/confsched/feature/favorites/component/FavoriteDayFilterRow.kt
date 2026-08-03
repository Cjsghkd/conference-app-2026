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
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.ui.safeClick

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
                onClick = safeClick { onDayFilterClick(null) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = optionCount),
                modifier = Modifier.width(FavoriteDayFilterRowDefaults.tabWidth),
            ) {
                Text("All")
            }
            DroidKaigi2026Day.entries.forEachIndexed { index, day ->
                SegmentedButton(
                    selected = selectedDayFilter == day,
                    onClick = safeClick { onDayFilterClick(day) },
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
