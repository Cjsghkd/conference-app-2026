package io.github.droidkaigi.confsched.feature.sessions.timetable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.safeClick
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.DayTabRow
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.TimetableListSection
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.TimetableListSectionUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    uiState: TimetableScreenUiState,
    onBookmarkClick: (TimetableItemId) -> Unit,
    onDayClick: (DroidKaigi2026Day) -> Unit,
    onItemClick: (TimetableItemId) -> Unit,
    onSearchClick: () -> Unit,
    onUiTypeChangeClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timetable", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = safeClick(onSearchClick)) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = safeClick(onUiTypeChangeClick)) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Switch to grid view")
                    }
                },
                // The root tab shell already insets its content; a second system-bar inset here would double it.
                windowInsets = WindowInsets(),
            )
        },
        contentWindowInsets = WindowInsets(),
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            DayTabRow(selectedDay = uiState.day, onDayClick = onDayClick)

            TimetableListSection(
                uiState = uiState.timetableListSection,
                onBookmarkClick = onBookmarkClick,
                onItemClick = onItemClick,
            )
        }
    }
}

private fun previewUiState() = TimetableScreenUiState(
    day = DroidKaigi2026Day.Day1,
    timetableListSection = TimetableListSectionUiState(
        timeSlots = persistentListOf(
            TimetableListSectionUiState.TimeSlot(
                startsAt = "10:00",
                endsAt = "10:40",
                items = persistentListOf(
                    TimetableItem(TimetableItemId("d1a"), "Compose Multiplatform in Practice", "Arctic Fox", "Sp1", DroidKaigi2026Day.Day1, "10:00", "10:40"),
                    TimetableItem(TimetableItemId("d1b"), "Themed previews without codegen", "Bumblebee", "Sp2", DroidKaigi2026Day.Day1, "10:00", "10:40"),
                ),
            ),
            TimetableListSectionUiState.TimeSlot(
                startsAt = "11:00",
                endsAt = "11:40",
                items = persistentListOf(
                    TimetableItem(TimetableItemId("d1c"), "Metro DI: graphs without Dagger", "Arctic Fox", "Sp3", DroidKaigi2026Day.Day1, "11:00", "11:40"),
                ),
            ),
        ),
        bookmarks = persistentSetOf(TimetableItemId("d1a")),
    ),
)

@Preview
@Composable
fun TimetableScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        TimetableScreen(
            uiState = previewUiState(),
            onBookmarkClick = {},
            onDayClick = {},
            onItemClick = {},
            onSearchClick = {},
            onUiTypeChangeClick = {},
        )
    }
}
