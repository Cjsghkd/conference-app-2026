package io.github.droidkaigi.confsched.feature.about

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.droidkaigi.confsched.core.common.UiScope
import io.github.droidkaigi.confsched.core.model.LicensesScreenScope

@GraphExtension(LicensesScreenScope::class)
interface LicensesScreenGraph {
    val screenContext: LicensesScreenContext

    val screenNavigator: LicensesScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(UiScope::class)
    fun interface Factory {
        fun createLicensesScreenGraph(): LicensesScreenGraph
    }
}
