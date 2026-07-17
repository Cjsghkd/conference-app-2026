package io.github.droidkaigi.confsched.feature.eventmap

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context

context(screenContext: EventMapScreenContext)
@Composable
fun EventMapScreenRoot() {
    val uiState = context(screenContext.presenterContext) {
        eventMapScreenPresenter()
    }
    EventMapScreen(uiState = uiState)
}
