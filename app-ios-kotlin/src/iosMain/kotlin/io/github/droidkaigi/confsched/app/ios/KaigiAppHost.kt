package io.github.droidkaigi.confsched.app.ios

import io.github.droidkaigi.confsched.app.IosAppGraph
import io.github.droidkaigi.confsched.app.RootTab
import io.github.droidkaigi.confsched.app.createIosAppGraph
import io.github.droidkaigi.confsched.app.kaigiAppViewController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import platform.UIKit.UIViewController

// Swift Export drops @Composable from the function types it bridges, so every declaration Swift
// reaches must stay free of Compose types; the graph is held privately for that reason.
class KaigiAppHost(swiftPackageLicensesJson: String) {

    private val graph: IosAppGraph = createIosAppGraph(swiftPackageLicensesJson)

    val currentTab: Flow<RootTabSelection?> = graph.rootTabNavigator.currentTab.map { tab ->
        tab?.let(::RootTabSelection)
    }

    fun initialize() {
        graph.appInitializer.initialize()
    }

    fun selectTab(tab: RootTab) {
        graph.rootTabNavigator.select(tab)
    }

    fun viewController(): UIViewController = kaigiAppViewController(graph)
}

// Swift Export's flow iterator casts every element through its class bridge, which a Kotlin enum
// (bridged as a Swift enum, a value type) fails at runtime; a class element crosses intact.
class RootTabSelection internal constructor(val tab: RootTab)
