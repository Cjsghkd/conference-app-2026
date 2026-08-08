package io.github.droidkaigi.confsched.feature.eventmap

import dev.zacsweers.metro.DependencyGraph
import io.github.droidkaigi.confsched.core.testing.TestingScope

@DependencyGraph(scope = TestingScope::class)
interface EventMapScreenTestGraph {
    val presenterContext: EventMapPresenterContext
}
