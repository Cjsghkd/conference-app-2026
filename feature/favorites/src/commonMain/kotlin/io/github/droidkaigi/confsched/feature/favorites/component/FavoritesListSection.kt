package io.github.droidkaigi.confsched.feature.favorites.component

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

@Composable
internal fun FavoritesListSection(
    uiState: FavoritesListSectionUiState,
    onBookmarkClick: (TimetableItemId) -> Unit,
    onItemClick: (TimetableItemId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        uiState.timeSlots.forEach { slot ->
            item(key = "${slot.day}-${slot.startsAt}-${slot.endsAt}") {
                // The day lives on each card's badge (unconditional, see FavoriteCard); the header stays time-only to avoid repeating it.
                Text(
                    text = "${slot.startsAt} - ${slot.endsAt}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(slot.items, key = { it.id.value }) { item ->
                FavoriteCard(
                    item = item,
                    onBookmarkClick = onBookmarkClick,
                    onClick = onItemClick,
                )
            }
        }
    }
}

@Preview
@Composable
fun FavoritesListSectionPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        FavoritesListSection(
            uiState = FavoritesListSectionUiState(
                timeSlots = persistentListOf(
                    FavoritesListSectionUiState.TimeSlot(
                        day = DroidKaigi2026Day.Day1,
                        startsAt = "10:00",
                        endsAt = "10:40",
                        items = persistentListOf(
                            TimetableItem(TimetableItemId("d1a"), "Compose Multiplatform in Practice", "Arctic Fox", "Sp1", DroidKaigi2026Day.Day1, "10:00", "10:40"),
                        ),
                    ),
                ),
            ),
            onBookmarkClick = {},
            onItemClick = {},
        )
    }
}
