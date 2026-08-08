package io.github.droidkaigi.confsched.core.model

import kotlinx.serialization.Serializable

/** The days the conference runs, with the date [label] each falls on. */
@Serializable
enum class DroidKaigi2026Day(val label: String) {
    Day1("9/2"),
    Day2("9/3"),
}
