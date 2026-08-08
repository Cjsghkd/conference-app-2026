package io.github.droidkaigi.confsched.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import io.github.droidkaigi.confsched.core.ui.KaigiNavigationBar
import io.github.droidkaigi.confsched.core.ui.KaigiNavigationBarItem
import io.github.droidkaigi.confsched.feature.about.AboutNavKey
import io.github.droidkaigi.confsched.feature.eventmap.EventMapNavKey
import io.github.droidkaigi.confsched.feature.favorites.FavoritesNavKey
import io.github.droidkaigi.confsched.feature.profilecard.ProfileCardNavKey
import io.github.droidkaigi.confsched.feature.sessions.timetable.TimetableNavKey

// Public and free of UI types so the iOS shell can drive tab selection through RootTabNavigator.
enum class RootTab(internal val key: NavKey) {
    Timetable(TimetableNavKey),
    EventMap(EventMapNavKey),
    Favorites(FavoritesNavKey),
    About(AboutNavKey),
    ProfileCard(ProfileCardNavKey),
}

private val RootTab.label: String
    get() = when (this) {
        RootTab.Timetable -> "Timetable"
        RootTab.EventMap -> "Event map"
        RootTab.Favorites -> "Favorites"
        RootTab.About -> "About"
        RootTab.ProfileCard -> "Profile"
    }

private val RootTab.icon: ImageVector
    get() = when (this) {
        RootTab.Timetable -> Icons.Filled.DateRange
        RootTab.EventMap -> Icons.Filled.Map
        RootTab.Favorites -> Icons.Filled.Favorite
        RootTab.About -> Icons.Filled.Info
        RootTab.ProfileCard -> Icons.Filled.AccountCircle
    }

private class RootTabSceneDecorator(
    private val currentKey: () -> NavKey?,
    private val onSelectTab: (RootTab) -> Unit,
) : SceneDecoratorStrategy<NavKey> {

    override fun SceneDecoratorStrategyScope<NavKey>.decorateScene(scene: Scene<NavKey>): Scene<NavKey> {
        val currentTab = RootTab.entries.firstOrNull { it.key == currentKey() }
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

    // The bar takes no layout space, so a scrollable destination must add
    // KaigiNavigationBarDefaults.occupiedHeight to its bottom content padding.
    override val content: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize()) {
            delegate.content()
            RootTabBar(
                currentTab = currentTab,
                onSelectTab = onSelectTab,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    override fun equals(other: Any?): Boolean =
        other is RootTabScene && delegate == other.delegate && currentTab == other.currentTab

    override fun hashCode(): Int = delegate.hashCode() * 31 + currentTab.hashCode()
}

/** The tab bar the shell shows under every root destination. */
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

private object RootTabBarSeeds {
    const val OUTLINE = 955
    const val FIRST_INDICATOR = OUTLINE + 1
}

@Composable
internal fun rememberRootTabSceneDecorator(
    currentKey: () -> NavKey?,
    onSelectTab: (RootTab) -> Unit,
): SceneDecoratorStrategy<NavKey> = remember(currentKey, onSelectTab) {
    RootTabSceneDecorator(currentKey, onSelectTab)
}
