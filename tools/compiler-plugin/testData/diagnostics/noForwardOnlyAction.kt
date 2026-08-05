package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.ActionEffect
import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenChannel

class SearchPresenterContext : PresenterContext

sealed interface SearchScreenAction {
    data object ItemClicked : SearchScreenAction
    data object Refreshed : SearchScreenAction
}

sealed interface SearchScreenActionResult {
    data object NavigateToDetail : SearchScreenActionResult
}

context(_: SearchPresenterContext)
@Composable
fun forwardOnlyPresenter(channel: ScreenChannel<SearchScreenAction, SearchScreenActionResult>) {
    ActionEffect(channel) { action ->
        when (action) {
            is SearchScreenAction.ItemClicked ->
                <!NO_FORWARD_ONLY_ACTION!>channel.emit(SearchScreenActionResult.NavigateToDetail)<!>
            is SearchScreenAction.Refreshed -> refresh()
        }
    }
}

context(_: SearchPresenterContext)
@Composable
fun okPresenter(channel: ScreenChannel<SearchScreenAction, SearchScreenActionResult>) {
    ActionEffect(channel) { action ->
        when (action) {
            is SearchScreenAction.ItemClicked -> refresh()
            is SearchScreenAction.Refreshed -> refresh()
        }
    }
}

fun refresh() {
}
