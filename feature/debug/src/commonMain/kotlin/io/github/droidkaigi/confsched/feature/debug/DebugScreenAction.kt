package io.github.droidkaigi.confsched.feature.debug

sealed interface DebugScreenAction {
    data object ClearData : DebugScreenAction
    data class SetSoilErrorOverlayEnabled(val enabled: Boolean) : DebugScreenAction
}
