package io.github.droidkaigi.confsched.feature.sessions.timetable

import io.github.droidkaigi.confsched.core.common.KaigiLogger
import kotlinx.coroutines.channels.Channel

class FakeKaigiLogger : KaigiLogger {
    val debugMessages = Channel<String>(Channel.UNLIMITED)

    override fun debug(message: () -> String) {
        debugMessages.trySend(message())
    }

    override fun info(message: () -> String) = Unit
    override fun warn(message: () -> String) = Unit
    override fun error(throwable: Throwable?, message: () -> String) = Unit
}
