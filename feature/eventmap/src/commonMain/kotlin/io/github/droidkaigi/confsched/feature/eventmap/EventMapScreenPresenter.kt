package io.github.droidkaigi.confsched.feature.eventmap

import androidx.compose.runtime.Composable

@Composable
context(_: EventMapPresenterContext)
fun eventMapScreenPresenter(): EventMapScreenUiState {
    return EventMapScreenUiState(
        title = "Event map",
    )
}
