package io.github.droidkaigi.confsched.feature.sessions.timetable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import io.github.droidkaigi.confsched.core.ui.safeClick
import io.github.droidkaigi.confsched.core.ui.safeClickable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
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
            DayTabs(selectedDay = uiState.day, onDayClick = onDayClick)

            TimetableList(
                timeSlots = uiState.timeSlots,
                bookmarks = uiState.bookmarks,
                onBookmarkClick = onBookmarkClick,
                onItemClick = onItemClick,
            )
        }
    }
}

@Composable
private fun DayTabs(
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
                    modifier = Modifier.width(TimetableDefaults.dayTabWidth),
                ) {
                    Text(day.name)
                }
            }
        }
    }
}

@Composable
private fun TimetableList(
    timeSlots: PersistentList<TimetableTimeSlot>,
    bookmarks: PersistentSet<TimetableItemId>,
    onBookmarkClick: (TimetableItemId) -> Unit,
    onItemClick: (TimetableItemId) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        timeSlots.forEach { slot ->
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
                    item = item,
                    isFavorite = item.id in bookmarks,
                    onBookmarkClick = onBookmarkClick,
                    onClick = onItemClick,
                )
            }
        }
    }
}

@Composable
private fun TimetableCard(
    item: TimetableItem,
    isFavorite: Boolean,
    onBookmarkClick: (TimetableItemId) -> Unit,
    onClick: (TimetableItemId) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().safeClickable { onClick(item.id) }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold)
                Text("${item.room} · ${item.speaker}")
            }
            IconButton(onClick = safeClick { onBookmarkClick(item.id) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
                )
            }
        }
    }
}

private object TimetableDefaults {
    val dayTabWidth = 104.dp
}

private fun previewUiState() = TimetableScreenUiState(
    day = DroidKaigi2026Day.Day1,
    timeSlots = persistentListOf(
        TimetableTimeSlot(
            startsAt = "10:00",
            endsAt = "10:40",
            items = persistentListOf(
                TimetableItem(TimetableItemId("d1a"), "Compose Multiplatform in Practice", "Arctic Fox", "Sp1", DroidKaigi2026Day.Day1, "10:00", "10:40"),
                TimetableItem(TimetableItemId("d1b"), "Themed previews without codegen", "Bumblebee", "Sp2", DroidKaigi2026Day.Day1, "10:00", "10:40"),
            ),
        ),
        TimetableTimeSlot(
            startsAt = "11:00",
            endsAt = "11:40",
            items = persistentListOf(
                TimetableItem(TimetableItemId("d1c"), "Metro DI: graphs without Dagger", "Arctic Fox", "Sp3", DroidKaigi2026Day.Day1, "11:00", "11:40"),
            ),
        ),
    ),
    bookmarks = persistentSetOf(TimetableItemId("d1a")),
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
