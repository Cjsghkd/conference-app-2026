package io.github.droidkaigi.confsched.feature.sessions.timetable

import io.github.droidkaigi.confsched.core.common.UserMessage
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet

sealed interface TimetableScreenAction {
    data class Bookmark(val id: TimetableItemId) : TimetableScreenAction
    data class SelectDay(val day: DroidKaigi2026Day) : TimetableScreenAction
}

sealed interface TimetableScreenActionResult {
    data class ShowMessage(val message: UserMessage) : TimetableScreenActionResult
}

data class TimetableScreenUiState(
    val day: DroidKaigi2026Day,
    val sessions: PersistentList<TimetableItem>,
    val bookmarks: PersistentSet<TimetableItemId>,
)
