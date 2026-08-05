package io.github.droidkaigi.confsched.core.common

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The app's source of the current time. The debug feature module replaces the binding with one the
 * debug tooling can shift.
 */
interface KaigiClock {
    fun now(): Instant
}

@Inject
@ContributesBinding(AppScope::class)
class SystemKaigiClock : KaigiClock {
    override fun now(): Instant = Clock.System.now()
}
