package io.github.droidkaigi.confsched.feature.eventmap

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context
import io.github.droidkaigi.confsched.core.common.retainScreenChannel

@Composable
context(screenContext: EventMapScreenContext)
fun EventMapScreenRoot() {
    val screenChannel = retainScreenChannel<EventMapScreenAction, Nothing>()

    val uiState = context(screenContext.presenterContext) {
        eventMapScreenPresenter(screenChannel = screenChannel)
    }
    EventMapScreen(
        uiState = uiState,
        onFloorClick = { screenChannel.send(EventMapScreenAction.SelectFloor(it)) },
    )
}
