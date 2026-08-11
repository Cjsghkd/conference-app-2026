package io.github.droidkaigi.confsched.jetwhale.protocol

import com.kitakkun.jetwhale.protocol.messaging.JetWhaleEvent
import com.kitakkun.jetwhale.protocol.messaging.JetWhaleRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A shortcut the app offers; the host renders one button per entry so both ends list the same instants. */
@SerialName("kaigiclock/preset")
@Serializable
data class KaigiClockPreset(
    val label: String,
    val epochMillis: Long,
)

@SerialName("kaigiclock/state")
@Serializable
data class KaigiClockState(
    val nowEpochMillis: Long,
    val offsetMillis: Long,
    val presets: List<KaigiClockPreset>,
)

@SerialName("kaigiclock/get_state")
@Serializable
data object GetKaigiClockState : JetWhaleRequest<KaigiClockState>

@SerialName("kaigiclock/shift_to")
@Serializable
data class ShiftKaigiClockTo(val targetEpochMillis: Long) : JetWhaleRequest<KaigiClockState>

@SerialName("kaigiclock/reset")
@Serializable
data object ResetKaigiClock : JetWhaleRequest<KaigiClockState>

/** Sent by the app whenever the offset changes, so a shift made on the debug screen reaches the host. */
@SerialName("kaigiclock/changed")
@Serializable
data class KaigiClockChanged(val state: KaigiClockState) : JetWhaleEvent
