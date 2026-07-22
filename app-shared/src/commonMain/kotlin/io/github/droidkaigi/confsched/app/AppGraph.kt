package io.github.droidkaigi.confsched.app

import io.github.droidkaigi.confsched.core.common.KaigiLogger
import io.github.droidkaigi.confsched.core.common.InitialNavKeyOverrideProvider
import io.github.droidkaigi.confsched.core.common.SoilDataContext
import io.github.droidkaigi.confsched.core.common.SoilErrorMonitor
import io.github.droidkaigi.confsched.core.common.MergedNavKeySerializersProvider
import io.github.droidkaigi.confsched.core.data.ThemeColorSchemeSubscriptionKey
import soil.query.SwrClientPlus

interface AppGraph : SoilDataContext {
    val uiGraphFactory: UiGraph.Factory

    val initialNavKeyOverrideProvider: InitialNavKeyOverrideProvider

    val logger: KaigiLogger
    val soilErrorMonitor: SoilErrorMonitor
    val rootTabNavigator: RootTabNavigator
    val navKeySerializersProvider: MergedNavKeySerializersProvider
    val swrClient: SwrClientPlus
    val themeColorSchemeSubscriptionKey: ThemeColorSchemeSubscriptionKey
}
