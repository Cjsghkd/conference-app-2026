package io.github.droidkaigi.confsched.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

// Connects the root tab bar to shells outside the Compose tree (the iOS app): publishes the
// current tab through RootTabNavigator (null = tab bar hidden) and routes native tab taps
// into the navigator.
@Composable
internal fun RootTabSyncEffect(
    backStack: NavBackStack<NavKey>,
    rootTabNavigator: RootTabNavigator,
    onSelectTab: (RootTab) -> Unit,
) {
    val currentTab = RootTab.entries.firstOrNull { it.key == backStack.lastOrNull() }
    LaunchedEffect(currentTab) {
        rootTabNavigator.updateCurrentTab(currentTab)
    }

    LaunchedEffect(Unit) {
        rootTabNavigator.selections.collect { tab -> onSelectTab(tab) }
    }
}
