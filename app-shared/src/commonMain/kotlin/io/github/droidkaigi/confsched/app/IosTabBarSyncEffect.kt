package io.github.droidkaigi.confsched.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.droidkaigi.confsched.core.common.PlatformOnly
import io.github.droidkaigi.confsched.core.common.TargetPlatform

// Connects the root tab bar to the native SwiftUI shell: publishes the current tab through
// RootTabNavigator (null = tab bar hidden) and routes native tab taps into the navigator.
@PlatformOnly(TargetPlatform.Ios)
@Composable
internal fun IosTabBarSyncEffect(
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
