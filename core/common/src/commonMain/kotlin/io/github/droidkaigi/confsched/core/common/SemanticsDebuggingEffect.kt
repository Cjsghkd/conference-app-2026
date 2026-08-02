package io.github.droidkaigi.confsched.core.common

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Exposes the enclosing composition's Compose roots to an external debugger, which both reads the
 * semantics tree and drives it — invoking a node's own actions — for as long as the call stays
 * composed. The debug feature module replaces the no-op binding to connect JetWhale.
 */
fun interface SemanticsDebuggingEffect {
    @Composable
    operator fun invoke()
}

@Inject
@ContributesBinding(AppScope::class)
class NoopSemanticsDebuggingEffect : SemanticsDebuggingEffect {
    @Composable
    override fun invoke() = Unit
}
