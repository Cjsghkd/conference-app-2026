package io.github.droidkaigi.confsched.feature.sessions.timetable

import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet

data class TimetableTimeSlot(
    val startsAt: String,
    val endsAt: String,
    val items: PersistentList<TimetableItem>,
)

data class TimetableScreenUiState(
    val day: DroidKaigi2026Day,
    val timeSlots: PersistentList<TimetableTimeSlot>,
    val bookmarks: PersistentSet<TimetableItemId>,
)
