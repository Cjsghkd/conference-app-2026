package io.github.droidkaigi.confsched.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/** The mark a room is identified by, drawn beside its name. */
enum class RoomShape { Circle, Star, Square, Triangle, Diamond }

/**
 * The palette a room carries wherever it appears: its chip, and anything tinted to match.
 *
 * Every value holds across all five schemes. The chip is filled with a fixed light color
 * and the card behind the accent is white, so the ground under a room's colors never
 * darkens and none of them need a second reading.
 */
@Immutable
data class RoomTheme(
    val container: Color,
    val onContainer: Color,
    val accent: Color,
    val shape: RoomShape,
)

/**
 * The palette for [room], matched on the room's name as the timetable reports it.
 *
 * A name the design has no palette for falls back to the Material scheme, so an unfamiliar
 * room still draws as a chip rather than disappearing.
 */
@Composable
@ReadOnlyComposable
fun roomTheme(room: String): RoomTheme {
    return when (room.uppercase()) {
        "NARWHAL" -> RoomTheme(
            container = Color(0xFFE2DCFE),
            onContainer = Color(0xFF3F2296),
            accent = Color(0xFF7B58CB),
            shape = RoomShape.Circle,
        )

        "OTTER" -> RoomTheme(
            container = Color(0xFFF0DCF8),
            onContainer = Color(0xFF6A1B9A),
            accent = Color(0xFFA341BD),
            shape = RoomShape.Star,
        )

        "PANDA" -> RoomTheme(
            container = Color(0xFFE3E9FB),
            onContainer = Color(0xFF3949AB),
            accent = Color(0xFF5566C4),
            shape = RoomShape.Square,
        )

        "QUAIL" -> RoomTheme(
            container = Color(0xFFD8F6E8),
            onContainer = Color(0xFF1B5E20),
            accent = Color(0xFF2E7D32),
            shape = RoomShape.Triangle,
        )

        "MEERKAT" -> RoomTheme(
            container = Color(0xFFDCF9FD),
            onContainer = Color(0xFF005A63),
            accent = Color(0xFF00838F),
            shape = RoomShape.Diamond,
        )

        else -> RoomTheme(
            container = MaterialTheme.colorScheme.surfaceContainer,
            onContainer = MaterialTheme.colorScheme.onSurface,
            accent = MaterialTheme.colorScheme.outline,
            shape = RoomShape.Circle,
        )
    }
}
