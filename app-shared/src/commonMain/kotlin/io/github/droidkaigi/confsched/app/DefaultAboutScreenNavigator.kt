package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.AppNavigator
import io.github.droidkaigi.confsched.core.common.DebugNavKeyProvider
import io.github.droidkaigi.confsched.core.model.AboutScreenScope
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.feature.about.AboutScreenNavigator
import io.github.droidkaigi.confsched.feature.sessions.timetable.TimetableItemDetailNavKey

@Inject
@SingleIn(AboutScreenScope::class)
@ContributesBinding(AboutScreenScope::class)
class DefaultAboutScreenNavigator(
    private val appNavigator: AppNavigator,
    private val debugNavKeyProvider: DebugNavKeyProvider,
) : AboutScreenNavigator {
    override fun openSessionDetail(id: TimetableItemId) {
        appNavigator.goTo(TimetableItemDetailNavKey(id))
    }

    override val isDebugMenuAvailable: Boolean get() = debugNavKeyProvider.debugNavKey != null

    override fun openDebug() {
        debugNavKeyProvider.debugNavKey?.let(appNavigator::goTo)
    }
}
