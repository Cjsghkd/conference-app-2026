package io.github.droidkaigi.confsched.app

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.AppScope
import io.github.droidkaigi.confsched.core.common.NavEntryProvider

@Inject
@SingleIn(AppScope::class)
class AppEntryProvider(private val providers: Set<NavEntryProvider>) {
    val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
        providers.forEach { provider ->
            with(provider) {
                register()
            }
        }
    }
}
