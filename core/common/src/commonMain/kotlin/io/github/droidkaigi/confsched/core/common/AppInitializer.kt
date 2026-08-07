package io.github.droidkaigi.confsched.core.common

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Runs process-wide startup work once, from the platform entry point, before the first composition.
 * The debug feature module replaces the no-op binding to connect JetWhale.
 */
fun interface AppInitializer {
    fun initialize()
}

@Inject
@ContributesBinding(AppScope::class)
class NoopAppInitializer : AppInitializer {
    override fun initialize() = Unit
}
