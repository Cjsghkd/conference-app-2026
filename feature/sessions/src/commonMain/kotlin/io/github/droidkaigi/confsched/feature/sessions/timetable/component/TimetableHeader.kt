package io.github.droidkaigi.confsched.feature.sessions.timetable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.KaigiIconButton
import io.github.droidkaigi.confsched.core.ui.KaigiTopAppBar

/** The title, the two actions and the day picker, all on the one dark band. */
@Composable
internal fun TimetableHeader(
    selectedDay: DroidKaigi2026Day,
    onDayClick: (DroidKaigi2026Day) -> Unit,
    onSearchClick: () -> Unit,
    onUiTypeChangeClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.inverseSurface)) {
        KaigiTopAppBar(title = "Timetable") {
            KaigiIconButton(seed = 777, onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
            KaigiIconButton(seed = 778, onClick = onUiTypeChangeClick) {
                Icon(Icons.Filled.DateRange, contentDescription = "Switch to grid view")
            }
        }
        DayTabRow(selectedDay = selectedDay, onDayClick = onDayClick)
    }
}

@Preview
@Composable
fun TimetableHeaderPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        TimetableHeader(
            selectedDay = DroidKaigi2026Day.Day1,
            onDayClick = {},
            onSearchClick = {},
            onUiTypeChangeClick = {},
        )
    }
}
