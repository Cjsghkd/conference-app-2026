package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import kotlin.math.roundToInt

@Composable
internal fun ShiftedClockBadge(now: String, offsetLabel: String) {
    // The area around the badge draws nothing and takes no pointer input, so only the badge's own
    // rectangle is in the way — and that rectangle can be dragged off whatever it covers. Material3's
    // Surface is avoided for the same reason: it installs a `pointerInput` that swallows taps.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(8.dp),
    ) {
        var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
        var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
        var badgeSize by remember { mutableStateOf(IntSize.Zero) }
        // Anchored top-end, so dragging runs left (negative) and down (positive) within the parent.
        // Both bounds stop at zero: a parent smaller than the badge would otherwise invert the range.
        val minX by rememberUpdatedState(minOf(badgeSize.width - constraints.maxWidth, 0).toFloat())
        val maxY by rememberUpdatedState(maxOf(constraints.maxHeight - badgeSize.height, 0).toFloat())

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                // Clamped on the way out as well as on drag: a window that shrinks after the badge was
                // moved would otherwise leave the saved position outside it, with nothing on screen.
                .offset {
                    IntOffset(
                        offsetX.coerceIn(minX, 0f).roundToInt(),
                        offsetY.coerceIn(0f, maxY).roundToInt(),
                    )
                }
                .onSizeChanged { badgeSize = it }
                // Keyed on nothing: the badge is re-measured every second as the time it shows
                // changes width, and a key that moved with it would tear the gesture down mid-drag.
                // The bounds are read through the state instead, so they stay current anyway.
                .pointerInput(Unit) {
                    detectDragGestures { change, delta ->
                        change.consume()
                        offsetX = (offsetX + delta.x).coerceIn(minX, 0f)
                        offsetY = (offsetY + delta.y).coerceIn(0f, maxY)
                    }
                }
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = now,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // The offset is the part that says the time is not the device's, so it keeps the warning colour.
            Text(
                text = offsetLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview
@Composable
private fun ShiftedClockBadgePreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        ShiftedClockBadge(now = "2026-09-02 10:00:00 JST", offsetLabel = "+2h 15m")
    }
}
