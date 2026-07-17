package io.github.droidkaigi.confsched.feature.debug

import io.github.droidkaigi.confsched.core.data.ServerEnvironment

sealed interface ServerEnvironmentScreenAction {
    data class SelectServer(val environment: ServerEnvironment) : ServerEnvironmentScreenAction
    data class SetSkipNextLaunch(val enabled: Boolean) : ServerEnvironmentScreenAction
}

sealed interface ServerEnvironmentScreenActionResult {
    data object ServerSelected : ServerEnvironmentScreenActionResult
}

data class ServerEnvironmentScreenUiState(
    val skipSelectionNextLaunch: Boolean,
    /** Non-null once preferences say the selection screen should be skipped with this environment. */
    val autoSelectEnvironment: ServerEnvironment?,
)
