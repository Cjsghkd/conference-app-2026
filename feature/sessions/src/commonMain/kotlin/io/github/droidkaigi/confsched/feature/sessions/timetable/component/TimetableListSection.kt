package io.github.droidkaigi.confsched.feature.sessions.timetable.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        uiState.timeSlots.forEach { slot ->
            item(key = "${slot.startsAt}-${slot.endsAt}") {
                Text(
                    text = "${slot.startsAt} - ${slot.endsAt}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(slot.items) { item ->
                TimetableCard(
                    title = item.title,
                    room = item.room,
                    speaker = item.speaker,
                    isFavorite = item.id in uiState.bookmarks,
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
            endsAt = "10:40",
            items = persistentListOf(
                previewItem("d1a", "Compose Multiplatform in Practice", "Speaker A", "10:00", "10:40"),
                previewItem("d1b", "Themed previews without codegen", "Speaker B", "10:00", "10:40"),
            ),
        ),
        TimetableListSectionUiState.TimeSlot(
            startsAt = "11:00",
            endsAt = "11:40",
            items = persistentListOf(
                previewItem("d1c", "Metro DI: graphs without Dagger", "Speaker C", "11:00", "11:40"),
            ),
        ),
    ),
    bookmarks = persistentSetOf(TimetableItemId("d1a")),
)

private fun previewItem(
    id: String,
    title: String,
    speaker: String,
    startsAt: String,
    endsAt: String,
) = TimetableItem(
    id = TimetableItemId(id),
    title = title,
    room = "Arctic Fox",
    speaker = speaker,
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
