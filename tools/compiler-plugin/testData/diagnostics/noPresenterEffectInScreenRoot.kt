package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.ActionEffect
import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenChannel
import io.github.droidkaigi.confsched.core.common.ScreenContext
import io.github.droidkaigi.confsched.core.common.context

class SearchPresenterContext : PresenterContext

class SearchScreenContext(val presenterContext: SearchPresenterContext) : ScreenContext

context(_: SearchPresenterContext)
@Composable
fun searchPresenter(channel: ScreenChannel<Int, Int>): Int = 0

context(screenContext: SearchScreenContext)
@Composable
fun SearchScreenRoot(channel: ScreenChannel<Int, Int>) {
    context(screenContext.presenterContext) {
        searchPresenter(channel)
        <!PRESENTER_EFFECT_IN_SCREEN_ROOT!>ActionEffect(channel) { }<!>
    }
}
