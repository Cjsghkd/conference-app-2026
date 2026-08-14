package io.github.droidkaigi.confsched.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.window.core.layout.WindowSizeClass
import io.github.droidkaigi.confsched.core.common.TargetPlatform
import io.github.droidkaigi.confsched.core.common.currentPlatform
import io.github.droidkaigi.confsched.core.ui.KaigiNavigationBar
import io.github.droidkaigi.confsched.core.ui.KaigiNavigationBarItem
import io.github.droidkaigi.confsched.core.ui.KaigiNavigationRail
import io.github.droidkaigi.confsched.core.ui.KaigiNavigationRailItem
import io.github.droidkaigi.confsched.core.ui.LocalNavigationBarOccupiedHeight
import io.github.droidkaigi.confsched.feature.about.AboutNavKey
import io.github.droidkaigi.confsched.feature.eventmap.EventMapNavKey
import io.github.droidkaigi.confsched.feature.favorites.FavoritesNavKey
import io.github.droidkaigi.confsched.feature.profilecard.ProfileCardNavKey
import io.github.droidkaigi.confsched.feature.sessions.timetable.TimetableNavKey

// Public and free of UI types so the iOS shell can drive tab selection through RootTabNavigator.
// The label names the destination to a screen reader on every platform; neither bar draws it.
enum class RootTab(internal val key: NavKey, val label: String) {
    Timetable(TimetableNavKey, "Timetable"),
    EventMap(EventMapNavKey, "Event map"),
    Favorites(FavoritesNavKey, "Favorites"),
    About(AboutNavKey, "About"),
    ProfileCard(ProfileCardNavKey, "Profile"),
}

private val RootTab.icon: ImageVector
    get() = when (this) {
        RootTab.Timetable -> Icons.Filled.DateRange
        RootTab.EventMap -> Icons.Filled.Map
        RootTab.Favorites -> Icons.Filled.Favorite
        RootTab.About -> Icons.Filled.Info
        RootTab.ProfileCard -> Icons.Filled.AccountCircle
    }

private val rootTabsByKey: Map<NavKey, RootTab> = RootTab.entries.associateBy(RootTab::key)

/**
 * The root tab a scene drawing [paneCount] entries still shows, or null when it shows none.
 *
 * Every scene this app forms draws the topmost [paneCount] entries of the back stack, so a
 * multi-pane scene keeps the entries below its top one on screen beside it: the list pane of a
 * list-detail scene is a root destination the reader is still looking at, and the tab it belongs to
 * stays the reader's place.
 */
private fun List<NavKey>.rootTabInTopPanes(paneCount: Int): RootTab? =
    takeLast(paneCount).asReversed().firstNotNullOfOrNull(rootTabsByKey::get)

private class RootTabSceneDecorator(
    private val backStack: List<NavKey>,
    private val onSelectTab: (RootTab) -> Unit,
) : SceneDecoratorStrategy<NavKey> {

    override fun SceneDecoratorStrategyScope<NavKey>.decorateScene(scene: Scene<NavKey>): Scene<NavKey> {
        val currentTab = backStack.rootTabInTopPanes(scene.entries.size)
            ?: return scene
        return RootTabScene(scene, currentTab, onSelectTab)
    }
}

private class RootTabScene(
    private val delegate: Scene<NavKey>,
    private val currentTab: RootTab,
    private val onSelectTab: (RootTab) -> Unit,
) : Scene<NavKey> {
    override val key: Any get() = delegate.key
    override val entries get() = delegate.entries
    override val previousEntries get() = delegate.previousEntries

    override val content: @Composable () -> Unit = {
        val isExpanded = currentWindowAdaptiveInfoV2().windowSizeClass
            .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
        val currentDelegate by rememberUpdatedState(delegate)
        val movableContent = remember { movableContentOf { currentDelegate.content() } }
        if (isExpanded) {
            Row(Modifier.fillMaxSize()) {
                RootTabRail(currentTab = currentTab, onSelectTab = onSelectTab)
                Box(Modifier.weight(1f)) {
                    CompositionLocalProvider(
                        LocalNavigationBarOccupiedHeight provides 0.dp,
                        content = movableContent,
                    )
                }
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                movableContent()
                RootTabBar(
                    currentTab = currentTab,
                    onSelectTab = onSelectTab,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean =
        other is RootTabScene && delegate == other.delegate && currentTab == other.currentTab

    override fun hashCode(): Int = delegate.hashCode() * 31 + currentTab.hashCode()
}

/** The tab bar the shell shows under every root destination on windows narrower than expanded. */
@Composable
private fun RootTabBar(
    currentTab: RootTab,
    onSelectTab: (RootTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    KaigiNavigationBar(outlineSeed = RootTabBarSeeds.OUTLINE, modifier = modifier) {
        RootTab.entries.forEachIndexed { index, tab ->
            KaigiNavigationBarItem(
                selected = tab == currentTab,
                onClick = { onSelectTab(tab) },
                indicatorSeed = RootTabBarSeeds.FIRST_INDICATOR + index,
            ) {
                Icon(tab.icon, contentDescription = tab.label)
            }
        }
    }
}

/** The tab rail the shell shows beside every root destination on expanded windows. */
@Composable
private fun RootTabRail(
    currentTab: RootTab,
    onSelectTab: (RootTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    KaigiNavigationRail(outlineSeed = RootTabBarSeeds.OUTLINE, modifier = modifier) {
        RootTab.entries.forEachIndexed { index, tab ->
            KaigiNavigationRailItem(
                selected = tab == currentTab,
                onClick = { onSelectTab(tab) },
                indicatorSeed = RootTabBarSeeds.FIRST_INDICATOR + index,
            ) {
                Icon(tab.icon, contentDescription = tab.label)
            }
        }
    }
}

private object RootTabBarSeeds {
    const val OUTLINE = 955
    const val FIRST_INDICATOR = OUTLINE + 1
}

// Null on iOS, where a SwiftUI bar draws natively above the Compose view controller.
@Composable
internal fun rememberRootTabSceneDecorator(
    backStack: NavBackStack<NavKey>,
    onSelectTab: (RootTab) -> Unit,
): SceneDecoratorStrategy<NavKey>? = if (currentPlatform == TargetPlatform.Ios) {
    null
} else {
    remember(backStack, onSelectTab) {
        RootTabSceneDecorator(backStack, onSelectTab)
    }
}
