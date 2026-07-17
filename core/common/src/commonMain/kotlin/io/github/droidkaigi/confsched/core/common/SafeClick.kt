package io.github.droidkaigi.confsched.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavEntryDecorator
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class SafeClickInvoker(
    private val timeSource: TimeSource = TimeSource.Monotonic,
    private val interval: Duration = 500.milliseconds,
) {
    private var lastAccepted: TimeMark? = null

    fun invoke(block: () -> Unit) {
        val previous = lastAccepted
        if (previous != null && previous.elapsedNow() < interval) return
        lastAccepted = timeSource.markNow()
        block()
    }
}

val LocalSafeClickInvoker = staticCompositionLocalOf { SafeClickInvoker() }

@Composable
fun <T : Any> rememberSafeClickInvokerNavEntryDecorator(): NavEntryDecorator<T> {
    return remember {
        NavEntryDecorator(
            decorate = { entry ->
                val invoker = remember { SafeClickInvoker() }
                CompositionLocalProvider(LocalSafeClickInvoker provides invoker) {
                    entry.Content()
                }
            },
        )
    }
}
