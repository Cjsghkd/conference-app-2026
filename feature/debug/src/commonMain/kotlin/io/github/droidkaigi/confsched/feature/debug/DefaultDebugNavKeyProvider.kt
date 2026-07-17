package io.github.droidkaigi.confsched.feature.debug

import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.droidkaigi.confsched.core.common.DebugNavKeyProvider
import io.github.droidkaigi.confsched.core.common.NoopDebugNavKeyProvider

@Inject
@ContributesBinding(AppScope::class, replaces = [NoopDebugNavKeyProvider::class])
class DefaultDebugNavKeyProvider : DebugNavKeyProvider {
    override val debugNavKey: NavKey = DebugNavKey
}
