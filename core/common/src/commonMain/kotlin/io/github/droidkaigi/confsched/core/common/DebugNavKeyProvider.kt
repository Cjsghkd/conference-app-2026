package io.github.droidkaigi.confsched.core.common

import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Provides the root NavKey of the debug menu screen.
 * The debug feature module replaces the no-op binding in builds that include it.
 */
interface DebugNavKeyProvider {
    /** Null when the build does not include the debug feature. */
    val debugNavKey: NavKey?
}

@Inject
@ContributesBinding(AppScope::class)
class NoopDebugNavKeyProvider : DebugNavKeyProvider {
    override val debugNavKey: NavKey? = null
}
