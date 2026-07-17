package io.github.droidkaigi.confsched.core.common

import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Overrides the app's initial destination. The debug feature module replaces the
 * no-op binding to open on its server picker.
 */
interface InitialNavKeyOverrideProvider {
    /** Null keeps the app's default initial destination. */
    val initialNavKeyOverride: NavKey?
}

@Inject
@ContributesBinding(AppScope::class)
class NoopInitialNavKeyOverrideProvider : InitialNavKeyOverrideProvider {
    override val initialNavKeyOverride: NavKey? = null
}
