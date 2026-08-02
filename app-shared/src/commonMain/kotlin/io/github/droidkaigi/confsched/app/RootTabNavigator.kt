package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

// Bridges the root tab bar to shells outside the Compose tree (the iOS app). Swift consumes
// `currentTab` as an AsyncSequence via Swift export (null = the tab bar is hidden) and calls
// `select` on tab taps; IosTabBarSyncEffect connects both ends to the Compose navigation state.
@Inject
@SingleIn(AppScope::class)
class RootTabNavigator {

    val currentTab: StateFlow<RootTab?>
        field = MutableStateFlow<RootTab?>(RootTab.entries.first())

    private val selectionChannel = Channel<RootTab>(Channel.BUFFERED)
    internal val selections: Flow<RootTab> = selectionChannel.receiveAsFlow()

    fun select(tab: RootTab) {
        selectionChannel.trySend(tab)
    }

    internal fun updateCurrentTab(tab: RootTab?) {
        currentTab.value = tab
    }
}
