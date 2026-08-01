package io.github.droidkaigi.confsched.feature.sessions.timetable.component

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
internal fun DayTabRow(
    selectedDay: DroidKaigi2026Day,
    onDayClick: (DroidKaigi2026Day) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        SingleChoiceSegmentedButtonRow {
            DroidKaigi2026Day.entries.forEachIndexed { index, day ->
                SegmentedButton(
                    selected = selectedDay == day,
                    onClick = safeClick { onDayClick(day) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = DroidKaigi2026Day.entries.size),
                    modifier = Modifier.width(DayTabRowDefaults.tabWidth),
                ) {
                    Text(day.name)
                }
            }
        }
    }
}

private object DayTabRowDefaults {
    val tabWidth = 104.dp
}
