package io.github.droidkaigi.confsched.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

// Connects the root tab bar to the SwiftUI shell: publishes the current tab through
// RootTabNavigator (null = tab bar hidden) and routes native tab taps into the navigator.
context(appGraph: IosAppGraph)
@Composable
fun IosTabBarSyncEffect(backStack: NavBackStack<NavKey>) {
    val currentTab = RootTab.entries.firstOrNull { it.key == backStack.lastOrNull() }
    LaunchedEffect(currentTab) {
        appGraph.rootTabNavigator.updateCurrentTab(currentTab)
    }

    LaunchedEffect(Unit) {
        appGraph.rootTabNavigator.selections.collect { tab ->
            appGraph.appNavigator.moveToTop(tab.key)
        }
    }
}
