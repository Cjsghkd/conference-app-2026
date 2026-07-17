package io.github.droidkaigi.confsched.feature.sessions.timetable

import io.github.droidkaigi.confsched.core.common.UserMessage
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId

sealed interface TimetableItemDetailScreenAction {
    data class Bookmark(val id: TimetableItemId) : TimetableItemDetailScreenAction
}

sealed interface TimetableItemDetailScreenActionResult {
    data class ShowMessage(val message: UserMessage) : TimetableItemDetailScreenActionResult
}

data class TimetableItemDetailScreenUiState(
    val item: TimetableItem,
    val isFavorite: Boolean,
)
