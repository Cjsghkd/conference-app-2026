package io.github.droidkaigi.confsched.feature.favorites.component

import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.TimetableItem
import kotlinx.collections.immutable.PersistentList

data class FavoritesListSectionUiState(
    val timeSlots: PersistentList<TimeSlot>,
) {
    data class TimeSlot(
        val day: DroidKaigi2026Day,
        val startsAt: String,
        val endsAt: String,
        val items: PersistentList<TimetableItem>,
    )
}
