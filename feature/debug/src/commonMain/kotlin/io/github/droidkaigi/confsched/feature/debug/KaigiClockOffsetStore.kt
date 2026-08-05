package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

@Inject
@SingleIn(AppScope::class)
class KaigiClockOffsetStore {
    val offset: StateFlow<Duration>
        field = MutableStateFlow(Duration.ZERO)

    // Measured against the system clock rather than the injected one, which already carries this
    // offset — reading it here would compound the shift on every call.
    fun shiftTo(target: Instant) {
        offset.value = target - Clock.System.now()
    }

    fun reset() {
        offset.value = Duration.ZERO
    }
}
