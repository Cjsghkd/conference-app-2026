package io.github.droidkaigi.confsched.feature.sessions.timetable.component

import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet

data class TimetableListSectionUiState(
    val timeSlots: PersistentList<TimeSlot>,
    val bookmarks: PersistentSet<TimetableItemId>,
) {
    data class TimeSlot(
        val startsAt: String,
        val endsAt: String,
        val items: PersistentList<TimetableItem>,
    )
}
