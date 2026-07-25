package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph

// Parameter-less (no Context factory) because Swift cannot call the reified createGraph<IosAppGraph>().
@DependencyGraph(scope = AppScope::class)
interface IosAppGraph : AppGraph

// Swift cannot call the reified createGraph<IosAppGraph>(); this is the Swift-facing entry point.
fun createIosAppGraph(): IosAppGraph = createGraph<IosAppGraph>()
