package io.github.droidkaigi.confsched.feature.staff

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import io.github.droidkaigi.confsched.core.common.ActionEffect
import io.github.droidkaigi.confsched.core.common.ScreenChannel

@Composable
context(_: StaffPresenterContext)
fun staffScreenPresenter(
    screenChannel: ScreenChannel<StaffScreenAction, StaffScreenActionResult>,
): StaffScreenUiState {
    var reloadCount by retain { mutableStateOf(0) }

    ActionEffect(screenChannel) { action ->
        when (action) {
            StaffScreenAction.Reload -> {
                reloadCount++
                screenChannel.emit(StaffScreenActionResult.Reloaded)
            }
        }
    }

    return StaffScreenUiState(
        title = "Staff",
        reloadCount = reloadCount,
    )
}
