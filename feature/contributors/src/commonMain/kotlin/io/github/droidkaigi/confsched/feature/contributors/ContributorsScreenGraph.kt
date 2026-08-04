package io.github.droidkaigi.confsched.feature.contributors

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.droidkaigi.confsched.core.common.UiScope
import io.github.droidkaigi.confsched.core.model.ContributorsScreenScope

@GraphExtension(ContributorsScreenScope::class)
interface ContributorsScreenGraph {
    val screenContext: ContributorsScreenContext

    val screenNavigator: ContributorsScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(UiScope::class)
    fun interface Factory {
        fun createContributorsScreenGraph(): ContributorsScreenGraph
    }
}
