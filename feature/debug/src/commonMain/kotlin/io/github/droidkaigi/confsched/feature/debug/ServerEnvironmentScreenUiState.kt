package io.github.droidkaigi.confsched.feature.debug

import io.github.droidkaigi.confsched.core.data.ServerEnvironment

data class ServerEnvironmentScreenUiState(
    val skipSelectionNextLaunch: Boolean,
    /** Non-null once preferences say the selection screen should be skipped with this environment. */
    val autoSelectEnvironment: ServerEnvironment?,
)
