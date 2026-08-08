package io.github.droidkaigi.confsched.feature.sessions.timetable

import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.preview.fake

data class TimetableItemDetailScreenUiState(
    val item: TimetableItem,
    val isFavorite: Boolean,
) {
    companion object
}

internal fun TimetableItemDetailScreenUiState.Companion.fake(): TimetableItemDetailScreenUiState = TimetableItemDetailScreenUiState(
    item = TimetableItem.fake(),
    isFavorite = true,
)
