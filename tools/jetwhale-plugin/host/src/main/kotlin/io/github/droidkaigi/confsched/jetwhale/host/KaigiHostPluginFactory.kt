package io.github.droidkaigi.confsched.jetwhale.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kitakkun.jetwhale.annotations.ExperimentalJetWhaleApi
import com.kitakkun.jetwhale.host.sdk.JetWhaleHostPlugin
import com.kitakkun.jetwhale.host.sdk.JetWhaleHostPluginFactory
import com.kitakkun.jetwhale.host.sdk.JetWhaleHostPluginUi
import com.kitakkun.jetwhale.host.sdk.JetWhaleMcpArgumentException
import com.kitakkun.jetwhale.host.sdk.JetWhaleMcpArguments
import com.kitakkun.jetwhale.host.sdk.JetWhaleMcpCapablePlugin
import com.kitakkun.jetwhale.host.sdk.JetWhaleMcpCommand
import com.kitakkun.jetwhale.host.sdk.JetWhaleMessagingHostPlugin
import com.kitakkun.jetwhale.protocol.messaging.JetWhaleMessageHandlers
import com.kitakkun.jetwhale.protocol.messaging.JetWhaleMessagingException
import com.kitakkun.jetwhale.protocol.messaging.request
import io.github.droidkaigi.confsched.jetwhale.protocol.GetKaigiClockState
import io.github.droidkaigi.confsched.jetwhale.protocol.KaigiClockChanged
import io.github.droidkaigi.confsched.jetwhale.protocol.KaigiClockState
import io.github.droidkaigi.confsched.jetwhale.protocol.ResetKaigiClock
import io.github.droidkaigi.confsched.jetwhale.protocol.ShiftKaigiClockTo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Instant

// Instantiated by the host via the fully-qualified name declared in plugin-manifest.json.
@Suppress("UNUSED")
class KaigiHostPluginFactory : JetWhaleHostPluginFactory {
    override fun createPlugin(): JetWhaleHostPlugin = KaigiHostPlugin()
}

@OptIn(ExperimentalJetWhaleApi::class)
private class KaigiHostPlugin :
    JetWhaleMessagingHostPlugin(),
    JetWhaleHostPluginUi,
    JetWhaleMcpCapablePlugin {

    private var clockState by mutableStateOf<KaigiClockState?>(null)
    private var lastError by mutableStateOf<String?>(null)
    private var pendingRequest: Job? = null

    override fun JetWhaleMessageHandlers.configure() {
        onEvent { event: KaigiClockChanged -> clockState = event.state }
    }

    override suspend fun onPrepare() {
        clockState = messenger.request(GetKaigiClockState)
    }

    @Composable
    override fun Content() {
        KaigiClockPluginView(
            state = clockState,
            error = lastError,
            onShiftTo = { epochMillis -> runOnPluginScope { messenger.request(ShiftKaigiClockTo(epochMillis)) } },
            onReset = { runOnPluginScope { messenger.request(ResetKaigiClock) } },
            onRefresh = { runOnPluginScope { messenger.request(GetKaigiClockState) } },
        )
    }

    // The newest click decides the clock, so an in-flight reply is abandoned rather than allowed to
    // land after it. The agent applied that earlier request all the same and reports the result as a
    // KaigiClockChanged event, so nothing is lost by dropping the reply.
    private fun runOnPluginScope(block: suspend () -> KaigiClockState) {
        pendingRequest?.cancel()
        pendingRequest = pluginScope.launch {
            // Catch the messaging failure specifically — runCatching would also swallow the
            // CancellationException that cancels this coroutine.
            try {
                clockState = block()
                lastError = null
            } catch (e: JetWhaleMessagingException) {
                lastError = e.message ?: e::class.simpleName ?: "The request failed"
            }
        }
    }

    override val mcpCommands: List<JetWhaleMcpCommand> = listOf(
        object : JetWhaleMcpCommand() {
            override val name = "io.github.droidkaigi.confsched2026.clock.getState"
            override val description = "Returns the app's current time and how far it is shifted from the system clock."

            override suspend fun execute(arguments: JetWhaleMcpArguments): String =
                messenger.request(GetKaigiClockState).toJson()
        },
        object : JetWhaleMcpCommand() {
            override val name = "io.github.droidkaigi.confsched2026.clock.setNow"
            override val description =
                "Shifts the app's clock so that it reads the given instant and keeps ticking from there."

            private val instant by string("The instant the app should read, in ISO-8601 (e.g. 2026-09-02T10:00:00+09:00).")

            override suspend fun execute(arguments: JetWhaleMcpArguments): String {
                val text = arguments[instant]
                val target = Instant.parseOrNull(text)
                    ?: throw JetWhaleMcpArgumentException("'$text' is not an ISO-8601 instant.")
                return messenger.request(ShiftKaigiClockTo(target.toEpochMilliseconds())).toJson()
            }
        },
        object : JetWhaleMcpCommand() {
            override val name = "io.github.droidkaigi.confsched2026.clock.reset"
            override val description = "Returns the app's clock to the system time."

            override suspend fun execute(arguments: JetWhaleMcpArguments): String =
                messenger.request(ResetKaigiClock).toJson()
        },
    )
}

private fun KaigiClockState.toJson(): String = buildJsonObject {
    put("now", Instant.fromEpochMilliseconds(nowEpochMillis).toString())
    put("offsetMillis", offsetMillis)
}.toString()
