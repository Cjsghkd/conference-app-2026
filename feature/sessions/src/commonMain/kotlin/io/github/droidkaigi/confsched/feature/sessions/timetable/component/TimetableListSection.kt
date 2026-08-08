package io.github.droidkaigi.confsched.feature.sessions.timetable.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.model.Language
import io.github.droidkaigi.confsched.core.model.Room
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.KaigiNavigationBarDefaults
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun TimetableListSection(
    uiState: TimetableListSectionUiState,
    onBookmarkClick: (TimetableItemId) -> Unit,
    onItemClick: (TimetableItemId) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(
            top = 24.dp,
            bottom = 24.dp + KaigiNavigationBarDefaults.occupiedHeight,
        ),
    ) {
        items(uiState.timeSlots, key = { "${it.startsAt}-${it.endsAt}" }) { slot ->
            SessionRow(
                slot = slot,
                bookmarks = uiState.bookmarks,
                onBookmarkClick = onBookmarkClick,
                onItemClick = onItemClick,
            )
        }
    }
}

/** One slot: when it runs, and the sessions running in it. */
@Composable
private fun SessionRow(
    slot: TimetableListSectionUiState.TimeSlot,
    bookmarks: Set<TimetableItemId>,
    onBookmarkClick: (TimetableItemId) -> Unit,
    onItemClick: (TimetableItemId) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TimetableTimeRange(
            startsAt = slot.startsAt,
            endsAt = slot.endsAt,
            seed = slot.startsAt.hashCode(),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            for (item in slot.items) {
                TimetableItemCard(
                    title = item.title,
                    room = item.room,
                    speaker = item.speaker,
                    language = item.language,
                    isFavorite = item.id in bookmarks,
                    seed = item.id.value.hashCode(),
                    onBookmarkClick = { onBookmarkClick(item.id) },
                    onClick = { onItemClick(item.id) },
                )
            }
        }
    }
}

private fun previewUiState() = TimetableListSectionUiState(
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
            ),
        ),
    ),
    bookmarks = persistentSetOf(TimetableItemId("d1a"), TimetableItemId("d1b")),
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
fun TimetableListSectionPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        TimetableListSection(
            uiState = previewUiState(),
            onBookmarkClick = {},
            onItemClick = {},
        )
    }
}
