package io.github.droidkaigi.confsched.feature.about

import io.github.droidkaigi.confsched.core.common.Navigator
import io.github.droidkaigi.confsched.core.model.TimetableItemId

interface AboutScreenNavigator : Navigator {
    fun openSessionDetail(id: TimetableItemId)

    fun openSponsors()

    fun openContributors()

    /** False when the build does not include the debug feature. */
    val isDebugMenuAvailable: Boolean
    fun openDebug()
}
