package io.github.droidkaigi.confsched.feature.staff

sealed interface StaffScreenActionResult {
    data object Reloaded : StaffScreenActionResult
}
