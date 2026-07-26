package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import io.github.droidkaigi.confsched.core.common.ActionEffect
import io.github.droidkaigi.confsched.core.common.MutationSuccessEffect
import io.github.droidkaigi.confsched.core.common.ScreenChannel
import io.github.droidkaigi.confsched.core.data.ServerEnvironmentSelection
import soil.query.compose.rememberMutation

@Composable
context(presenterContext: ServerEnvironmentPresenterContext)
fun serverEnvironmentScreenPresenter(
    screenChannel: ScreenChannel<ServerEnvironmentScreenAction, ServerEnvironmentScreenActionResult>,
): ServerEnvironmentScreenUiState {
    val selectionMutation = rememberMutation(presenterContext.serverEnvironmentSelectionMutationKey)
    var skipNextLaunch by retain { mutableStateOf(false) }

    val persistedSkip by presenterContext.debugPreferencesStore.skipServerSelection
        .collectAsState(initial = null)
    val persistedEnvironment by presenterContext.debugPreferencesStore.serverEnvironment
        .collectAsState(initial = null)

    // Reflect the persisted choice once loaded so re-visiting the screen shows the saved state.
    LaunchedEffect(persistedSkip) {
        persistedSkip?.let { skipNextLaunch = it }
    }

    ActionEffect(screenChannel) { action ->
        when (action) {
            is ServerEnvironmentScreenAction.SetSkipNextLaunch -> skipNextLaunch = action.enabled

            is ServerEnvironmentScreenAction.SelectServer -> selectionMutation.mutateAsync(
                ServerEnvironmentSelection(
                    environment = action.environment,
                    skipSelectionNextLaunch = skipNextLaunch,
                ),
            )
        }
    }

    MutationSuccessEffect(selectionMutation) {
        screenChannel.emit(ServerEnvironmentScreenActionResult.ServerSelected)
        selectionMutation.reset()
    }

    return ServerEnvironmentScreenUiState(
        skipSelectionNextLaunch = skipNextLaunch,
        autoSelectEnvironment = persistedEnvironment.takeIf { persistedSkip == true },
    )
}
