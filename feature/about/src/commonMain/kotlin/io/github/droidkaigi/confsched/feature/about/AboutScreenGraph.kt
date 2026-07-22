package io.github.droidkaigi.confsched.feature.about

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.droidkaigi.confsched.core.common.UiScope
import io.github.droidkaigi.confsched.core.model.AboutScreenScope

@GraphExtension(AboutScreenScope::class)
interface AboutScreenGraph {
    val screenContext: AboutScreenContext

    val screenNavigator: AboutScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(UiScope::class)
    fun interface Factory {
        fun createAboutScreenGraph(): AboutScreenGraph
    }
}
