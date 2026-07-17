package io.github.droidkaigi.confsched.feature.about

import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.AppScope
import io.github.droidkaigi.confsched.core.common.NavEntryProvider
import io.github.droidkaigi.confsched.core.common.context
import io.github.droidkaigi.confsched.core.common.instantNavTransition

@ContributesIntoSet(AppScope::class)
@Inject
class AboutNavEntryProvider(
    private val screenGraphFactory: AboutScreenGraph.Factory,
) : NavEntryProvider {
    override fun EntryProviderScope<NavKey>.register() {
        entry<AboutNavKey>(metadata = instantNavTransition()) {
            val graph = retain(screenGraphFactory::createAboutScreenGraph)
            context(graph.screenContext) {
                AboutScreenRoot(
                    onNavigateToSessionDetail = graph.screenNavigator::openSessionDetail,
                    isDebugMenuAvailable = graph.screenNavigator.isDebugMenuAvailable,
                    onNavigateToDebug = graph.screenNavigator::openDebug,
                )
            }
        }
    }
}
