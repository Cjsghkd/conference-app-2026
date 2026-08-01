package io.github.droidkaigi.confsched.feature.sessions.timetable

import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.TimetableListSectionUiState

data class TimetableScreenUiState(
    val day: DroidKaigi2026Day,
    val timetableListSection: TimetableListSectionUiState,
)
