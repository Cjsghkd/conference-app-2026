package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.droidkaigi.confsched.core.model.SoilErrorsScreenScope

@GraphExtension(SoilErrorsScreenScope::class)
interface SoilErrorsScreenGraph {
    val screenContext: SoilErrorsScreenContext

    @GraphExtension.Factory
    @ContributesTo(AppScope::class)
    fun interface Factory {
        fun createSoilErrorsScreenGraph(): SoilErrorsScreenGraph
    }
}
