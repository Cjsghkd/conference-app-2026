package io.github.droidkaigi.confsched.feature.eventmap

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.droidkaigi.confsched.core.model.EventMapScreenScope

@GraphExtension(EventMapScreenScope::class)
interface EventMapScreenGraph {
    val screenContext: EventMapScreenContext
    val screenNavigator: EventMapScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(AppScope::class)
    fun interface Factory {
        fun createEventMapScreenGraph(): EventMapScreenGraph
    }
}
