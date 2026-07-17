package io.github.droidkaigi.confsched.feature.eventmap

import androidx.compose.runtime.Composable

context(_: EventMapPresenterContext)
@Composable
fun eventMapScreenPresenter(): EventMapScreenUiState {
    return EventMapScreenUiState(
        title = "Event map",
    )
}
