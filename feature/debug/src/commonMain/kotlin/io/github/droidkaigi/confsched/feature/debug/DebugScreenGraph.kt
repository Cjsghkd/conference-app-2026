package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import io.github.droidkaigi.confsched.core.model.DebugScreenScope
import io.github.droidkaigi.confsched.core.model.MutationTag

@GraphExtension(DebugScreenScope::class)
interface DebugScreenGraph {
    val screenContext: DebugScreenContext

    @Provides
    private fun provideMutationTag(): MutationTag = MutationTag("DebugScreen")

    @GraphExtension.Factory
    @ContributesTo(AppScope::class)
    fun interface Factory {
        fun createDebugScreenGraph(): DebugScreenGraph
    }
}
