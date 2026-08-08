package io.github.droidkaigi.confsched.core.testing

import kotlinx.coroutines.channels.Channel

// A MutationKey fake delegates to buildMutationKey, whose expression cannot reference the class
// under construction — the mutable behaviour therefore lives in a constructor parameter.
class FakeMutationState<S> {
    val invocations = Channel<S>(Channel.UNLIMITED)
    private var failure: Throwable? = null

    suspend fun record(value: S) {
        invocations.send(value)
        failure?.let { throw it }
    }

    fun failWith(throwable: Throwable) {
        failure = throwable
    }
}
