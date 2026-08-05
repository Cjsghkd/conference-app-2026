package io.github.droidkaigi.confsched.feature.debug

import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import kotlin.time.Instant

enum class DebugClockPreset(val label: String, val instant: Instant) {
    Day1Morning("Day 1 10:00", DroidKaigi2026Day.Day1.at(hour = 10, minute = 0)),
    Day1Evening("Day 1 18:00", DroidKaigi2026Day.Day1.at(hour = 18, minute = 0)),
    Day2Morning("Day 2 10:00", DroidKaigi2026Day.Day2.at(hour = 10, minute = 0)),
}
