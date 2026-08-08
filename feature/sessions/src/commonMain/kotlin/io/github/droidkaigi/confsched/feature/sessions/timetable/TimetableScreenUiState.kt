package io.github.droidkaigi.confsched.feature.sessions.timetable

import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.TimetableListSectionUiState
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.fake

data class TimetableScreenUiState(
    val day: DroidKaigi2026Day,
    val timetableListSection: TimetableListSectionUiState,
) {
    companion object
}

internal fun TimetableScreenUiState.Companion.fake(): TimetableScreenUiState = TimetableScreenUiState(
    day = DroidKaigi2026Day.Day1,
    timetableListSection = TimetableListSectionUiState.fake(),
)
