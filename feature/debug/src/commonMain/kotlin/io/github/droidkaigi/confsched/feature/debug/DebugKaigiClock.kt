package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.KaigiClock
import io.github.droidkaigi.confsched.core.common.SystemKaigiClock
import kotlin.time.Clock
import kotlin.time.Instant

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, replaces = [SystemKaigiClock::class])
class DebugKaigiClock(
    private val offsetStore: KaigiClockOffsetStore,
) : KaigiClock {
    override fun now(): Instant = Clock.System.now() + offsetStore.offset.value
}
