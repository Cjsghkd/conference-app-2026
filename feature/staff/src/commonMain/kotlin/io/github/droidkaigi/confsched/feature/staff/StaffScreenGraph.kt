package io.github.droidkaigi.confsched.feature.staff

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.droidkaigi.confsched.core.common.UiScope
import io.github.droidkaigi.confsched.core.model.StaffScreenScope

@GraphExtension(StaffScreenScope::class)
interface StaffScreenGraph {
    val screenContext: StaffScreenContext

    val screenNavigator: StaffScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(UiScope::class)
    fun interface Factory {
        fun createStaffScreenGraph(): StaffScreenGraph
    }
}
