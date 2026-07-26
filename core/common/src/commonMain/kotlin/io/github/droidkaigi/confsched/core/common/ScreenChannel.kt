package io.github.droidkaigi.confsched.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.retain.retain
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class ScreenChannel<Action, ActionResult>(
    internal val actions: Channel<Action> = Channel(Channel.BUFFERED),
    internal val results: Channel<ActionResult> = Channel(Channel.BUFFERED),
) {
    context(_: ScreenContext)
    fun send(action: Action) {
        actions.trySend(action)
    }

    context(_: PresenterContext)
    suspend fun emit(result: ActionResult) {
        results.send(result)
    }
}

// Retained (not remembered) so buffered, not-yet-consumed actions/results survive transient
// destruction of the entry instead of being dropped with a recreated channel.
@Composable
fun <A, R> retainScreenChannel(): ScreenChannel<A, R> = retain { ScreenChannel() }

@Composable
context(_: PresenterContext)
fun <A> ActionEffect(channel: ScreenChannel<A, *>, block: suspend (A) -> Unit) {
    LaunchedEffect(channel) {
        channel.actions.receiveAsFlow().collect { block(it) }
    }
}

@Composable
context(_: ScreenContext)
fun <R> ActionResultEffect(channel: ScreenChannel<*, R>, block: suspend (R) -> Unit) {
    LaunchedEffect(channel) {
        channel.results.receiveAsFlow().collect { block(it) }
    }
}
