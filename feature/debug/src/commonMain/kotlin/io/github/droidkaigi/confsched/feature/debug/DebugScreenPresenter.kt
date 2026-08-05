package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import io.github.droidkaigi.confsched.core.common.ActionEffect
import io.github.droidkaigi.confsched.core.common.ScreenChannel
import io.github.droidkaigi.confsched.core.model.ConferenceTimeZone
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime
import soil.query.compose.rememberMutation
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Composable
context(presenterContext: DebugPresenterContext)
fun debugScreenPresenter(
    screenChannel: ScreenChannel<DebugScreenAction, DebugScreenActionResult>,
): DebugScreenUiState {
    val soilErrorOverlayMutation = rememberMutation(presenterContext.soilErrorOverlayEnabledMutationKey)
    var dataCleared by retain { mutableStateOf(false) }
    val soilErrorOverlayEnabled by presenterContext.debugPreferencesStore
        .soilErrorOverlayEnabled.collectAsState(initial = true)
    val soilErrors by presenterContext.soilErrorMonitor.errors.collectAsState()
    val clockOffset by presenterContext.clockOffsetStore.offset.collectAsState()
    var invalidClockInput by retain { mutableStateOf(false) }
    var now by retain { mutableStateOf(presenterContext.clock.now()) }

    // Keyed on the offset so a shift shows up immediately instead of at the next tick.
    LaunchedEffect(clockOffset) {
        while (true) {
            now = presenterContext.clock.now()
            delay(1.seconds)
        }
    }

    ActionEffect(screenChannel) { action ->
        when (action) {
            DebugScreenAction.ClearData -> {
                presenterContext.persistedDataResetter.clearAll()
                dataCleared = true
            }

            is DebugScreenAction.SetSoilErrorOverlayEnabled ->
                soilErrorOverlayMutation.mutateAsync(action.enabled)

            is DebugScreenAction.ApplyClockPreset -> {
                presenterContext.clockOffsetStore.shiftTo(action.preset.instant)
                invalidClockInput = false
            }

            is DebugScreenAction.ShiftClockTo -> {
                val target = Instant.parseOrNull(action.isoInstant)
                if (target == null) {
                    invalidClockInput = true
                } else {
                    presenterContext.clockOffsetStore.shiftTo(target)
                    invalidClockInput = false
                }
            }

            DebugScreenAction.ResetClock -> {
                presenterContext.clockOffsetStore.reset()
                invalidClockInput = false
            }
        }
    }

    return DebugScreenUiState(
        appVersion = presenterContext.buildConfig.versionName,
        dataCleared = dataCleared,
        soilErrorOverlayEnabled = soilErrorOverlayEnabled,
        soilErrors = soilErrors,
        clock = DebugClockUiState(
            now = now.formatInConferenceTime(),
            offsetLabel = clockOffset.toOffsetLabel(),
            shifted = clockOffset != Duration.ZERO,
            invalidInput = invalidClockInput,
        ),
    )
}

private val conferenceTimeFormat: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
    year()
    chars("-")
    monthNumber()
    chars("-")
    day()
    chars(" ")
    hour()
    chars(":")
    minute()
    chars(":")
    second()
}

private fun Instant.formatInConferenceTime(): String =
    "${toLocalDateTime(ConferenceTimeZone).format(conferenceTimeFormat)} JST"

private fun Duration.toOffsetLabel(): String = when {
    this == Duration.ZERO -> "System time"
    isPositive() -> "+$this"
    else -> toString()
}
