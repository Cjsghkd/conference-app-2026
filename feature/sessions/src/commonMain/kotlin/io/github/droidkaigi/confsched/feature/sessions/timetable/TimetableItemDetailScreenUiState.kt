package io.github.droidkaigi.confsched.feature.sessions.timetable

import io.github.droidkaigi.confsched.core.model.TimetableItem

data class TimetableItemDetailScreenUiState(
    val item: TimetableItem,
    val isFavorite: Boolean,
)
