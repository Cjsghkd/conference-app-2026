package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory

@DependencyGraph(scope = AppScope::class)
interface IosAppGraph : AppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides @SwiftPackageLicenses swiftPackageLicensesJson: String): IosAppGraph
    }
}

// Swift cannot call the reified createGraphFactory<IosAppGraph.Factory>(); this is the Swift-facing
// entry point. The Swift packages Xcode links are described by the iOS build, not by Gradle, so
// their export arrives from the caller rather than from a resource this module owns.
fun createIosAppGraph(swiftPackageLicensesJson: String): IosAppGraph =
    createGraphFactory<IosAppGraph.Factory>().create(swiftPackageLicensesJson)
