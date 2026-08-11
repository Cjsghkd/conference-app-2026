package io.github.droidkaigi.confsched.feature.staff

sealed interface StaffScreenAction {
    data object Reload : StaffScreenAction
}
