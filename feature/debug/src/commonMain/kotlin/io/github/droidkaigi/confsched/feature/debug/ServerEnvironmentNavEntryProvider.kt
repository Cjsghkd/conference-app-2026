package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.retain.retain
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.github.droidkaigi.confsched.core.common.NavEntryProvider
import io.github.droidkaigi.confsched.core.common.UiScope
import io.github.droidkaigi.confsched.core.common.context

@ContributesIntoSet(UiScope::class)
@Inject
class ServerEnvironmentNavEntryProvider(
    private val screenGraphFactory: ServerEnvironmentScreenGraph.Factory,
    private val screenNavigator: ServerEnvironmentScreenNavigator,
) : NavEntryProvider {

    override fun EntryProviderScope<NavKey>.register() {
        entry<ServerEnvironmentNavKey> {
            val graph = retain(screenGraphFactory::createServerEnvironmentScreenGraph)
            context(graph.screenContext) {
                ServerEnvironmentScreenRoot(
                    onNavigateToTimetable = screenNavigator::openTimetable,
                )
            }
        }
    }
}
