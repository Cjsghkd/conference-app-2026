package io.github.droidkaigi.confsched.core.common

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Exposes [backStack] to an external debugger, which both reads it and drives it — push, pop,
 * reorder — for as long as the call stays composed. Goes next to the `NavDisplay` that renders the
 * stack. The debug feature module replaces the no-op binding to connect JetWhale.
 */
fun interface BackStackDebuggingEffect {
    @Composable
    operator fun invoke(backStack: NavBackStack<NavKey>)
}

@Inject
@ContributesBinding(AppScope::class)
class NoopBackStackDebuggingEffect : BackStackDebuggingEffect {
    @Composable
    override fun invoke(backStack: NavBackStack<NavKey>) = Unit
}
