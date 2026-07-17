package io.github.droidkaigi.confsched.feature.sessions

import io.github.droidkaigi.confsched.core.common.SafeClickInvoker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource

@OptIn(ExperimentalTime::class)
class SafeClickInvokerTest {

    @Test
    fun dropsSecondCallWithinInterval_thenPassesAfterInterval() {
        val timeSource = TestTimeSource()
        val invoker = SafeClickInvoker(timeSource = timeSource, interval = 500.milliseconds)
        var count = 0

        invoker.invoke { count++ }
        assertEquals(1, count, "first call runs")

        timeSource += 200.milliseconds
        invoker.invoke { count++ }
        assertEquals(1, count, "second call within 500ms is dropped")

        timeSource += 400.milliseconds
        invoker.invoke { count++ }
        assertEquals(2, count, "call after 500ms passes")
    }
}
