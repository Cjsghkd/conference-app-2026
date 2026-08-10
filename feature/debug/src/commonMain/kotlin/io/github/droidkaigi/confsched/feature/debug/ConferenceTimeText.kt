package io.github.droidkaigi.confsched.feature.debug

import io.github.droidkaigi.confsched.core.model.ConferenceTimeZone
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

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

internal fun Instant.formatInConferenceTime(): String =
    "${toLocalDateTime(ConferenceTimeZone).format(conferenceTimeFormat)} JST"

// Rounded to whole seconds: a shift is set to the second at most, and the sub-second remainder the
// system clock contributes would otherwise fill the label with digits that mean nothing.
internal fun Duration.toOffsetLabel(): String {
    if (this == Duration.ZERO) return "System time"
    val whole = inWholeSeconds.seconds
    return if (whole.isNegative()) whole.toString() else "+$whole"
}
