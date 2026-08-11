package io.github.droidkaigi.confsched.feature.staff

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.ActionResultEffect
import io.github.droidkaigi.confsched.core.common.LocalSnackbarHostState
import io.github.droidkaigi.confsched.core.common.context
import io.github.droidkaigi.confsched.core.common.retainScreenChannel

@Composable
context(screenContext: StaffScreenContext)
fun StaffScreenRoot(
    onNavigateBack: () -> Unit,
) {
    val screenChannel = retainScreenChannel<StaffScreenAction, StaffScreenActionResult>()
    val snackbarHostState = LocalSnackbarHostState.current

    ActionResultEffect(screenChannel) { result ->
        when (result) {
            StaffScreenActionResult.Reloaded -> snackbarHostState.showSnackbar("Reloaded")
        }
    }

    val uiState = context(screenContext.presenterContext) {
        staffScreenPresenter(screenChannel)
    }
    StaffScreen(
        uiState = uiState,
        onReloadClick = { screenChannel.send(StaffScreenAction.Reload) },
        onBackClick = onNavigateBack,
    )
}
