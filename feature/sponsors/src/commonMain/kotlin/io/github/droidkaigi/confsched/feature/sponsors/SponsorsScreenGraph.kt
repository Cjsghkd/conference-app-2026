package io.github.droidkaigi.confsched.feature.sponsors

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.droidkaigi.confsched.core.common.UiScope
import io.github.droidkaigi.confsched.core.model.SponsorsScreenScope

@GraphExtension(SponsorsScreenScope::class)
interface SponsorsScreenGraph {
    val screenContext: SponsorsScreenContext

    val screenNavigator: SponsorsScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(UiScope::class)
    fun interface Factory {
        fun createSponsorsScreenGraph(): SponsorsScreenGraph
    }
}
