package io.github.droidkaigi.confsched.feature.sessions.timetable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.model.Language
import io.github.droidkaigi.confsched.core.model.Room
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.TimetableHeader
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.TimetableListSection
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.TimetableListSectionUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Composable
fun TimetableScreen(
    uiState: TimetableScreenUiState,
    onBookmarkClick: (TimetableItemId) -> Unit,
    onDayClick: (DroidKaigi2026Day) -> Unit,
    onItemClick: (TimetableItemId) -> Unit,
    onSearchClick: () -> Unit,
    onUiTypeChangeClick: () -> Unit,
) {
    Scaffold(contentWindowInsets = WindowInsets()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TimetableHeader(
                selectedDay = uiState.day,
                onDayClick = onDayClick,
                onSearchClick = onSearchClick,
                onUiTypeChangeClick = onUiTypeChangeClick,
            )
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
                endsAt = "10:20",
                items = persistentListOf(
                    previewItem("d1a", "Welcome Talk", Room.NARWHAL, "", Language.MIXED, "10:00", "10:20"),
                ),
            ),
            TimetableListSectionUiState.TimeSlot(
                startsAt = "11:00",
                endsAt = "11:40",
                items = persistentListOf(
                    previewItem("d1b", "DroidKaigiアプリで見るアーキテクチャの変遷", Room.OTTER, "Speaker B", Language.ENGLISH, "11:00", "11:40"),
                    previewItem("d1c", "CIパイプラインの最適化戦略", Room.PANDA, "Speaker C", Language.ENGLISH, "11:00", "11:40"),
                ),
            ),
            TimetableListSectionUiState.TimeSlot(
                startsAt = "13:00",
                endsAt = "13:45",
                items = persistentListOf(
                    previewItem("d1d", "Kotlin Multiplatform: State of the Union", Room.QUAIL, "Speaker D", Language.ENGLISH, "13:00", "13:45"),
                    previewItem("d1e", "Jetpack Composeのパフォーマンスチューニング", Room.MEERKAT, "Speaker E", Language.ENGLISH, "13:00", "13:45"),
                ),
            ),
        ),
        bookmarks = persistentSetOf(TimetableItemId("d1a"), TimetableItemId("d1b")),
    ),
)

private fun previewItem(
    id: String,
    title: String,
    room: Room,
    speaker: String,
    language: Language,
    startsAt: String,
    endsAt: String,
) = TimetableItem(
    id = TimetableItemId(id),
    title = title,
    room = room,
    speaker = speaker,
    language = language,
    day = DroidKaigi2026Day.Day1,
    startsAt = startsAt,
    endsAt = endsAt,
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
