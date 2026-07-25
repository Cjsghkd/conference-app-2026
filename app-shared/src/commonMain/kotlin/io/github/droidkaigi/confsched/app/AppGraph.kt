package io.github.droidkaigi.confsched.app

import io.github.droidkaigi.confsched.core.common.SoilDataContext

interface AppGraph : SoilDataContext {
    val uiGraphFactory: UiGraph.Factory

    val rootTabNavigator: RootTabNavigator
}
