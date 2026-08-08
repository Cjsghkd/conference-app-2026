package io.github.droidkaigi.confsched.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme

/** How finely the tremor octave ripples: shorter is a faster, tighter shake. */
private val DefaultTremorWavelength = 42.dp

/** How far apart the broad sweep turns: the design spec recommends 300dp. */
private val DefaultSweepWavelength = 300.dp
private val DefaultRoughness = 1.dp
private val DefaultTremor = 0.3.dp

// Swept as the horizontal axis of both taste grids, so the divider grid and the
// border grid can be read against each other.
private val TREMOR_STEPS = listOf(0.dp, 0.3.dp, 1.dp)

/**
 * A horizontal rule drawn as a single hand-sketched stroke.
 *
 * The same [seed] always produces the same line, so the stroke stays stable
 * across recompositions; use a different [seed] per call site to avoid two
 * neighbouring dividers sharing an identical wobble.
 *
 * [roughness] sets how far the broad sweep departs from a straight line, [tremor]
 * the swing of the finer wobble laid over it, and [tremorWavelength] how tightly
 * that wobble ripples.
 */
@Composable
fun SketchDivider(
    seed: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline,
    thickness: Dp = 2.dp,
    roughness: Dp = DefaultRoughness,
    tremor: Dp = DefaultTremor,
    sweepWavelength: Dp = DefaultSweepWavelength,
    tremorWavelength: Dp = DefaultTremorWavelength,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness + (roughness + tremor) * 2)
            .drawWithCache {
                val path = sketchLinePath(
                    width = size.width,
                    centerY = size.height / 2f,
                    roughness = roughness,
                    tremor = tremor,
                    sweepWavelength = sweepWavelength,
                    tremorWavelength = tremorWavelength,
                    seed = seed,
                )
                val stroke = Stroke(width = thickness.toPx(), cap = StrokeCap.Round)
                onDrawBehind {
                    drawPath(path = path, color = color, style = stroke)
                }
            },
    )
}

/**
 * A vertical wavy line, for a timeline running down a column.
 *
 * The caller sets the height; the width follows from [amplitude] and [thickness].
 * [noiseAmount] decides how mechanical the ripple looks, from an even sine at `0`
 * to visibly uneven crests at `1`.
 */
@Composable
fun SketchWavyLine(
    seed: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline,
    thickness: Dp = 1.dp,
    amplitude: Dp = 3.dp,
    wavelength: Dp = 10.dp,
    noiseAmount: Float = 0.8f,
) {
    Spacer(
        modifier = modifier
            .width(amplitude * 2 + thickness)
            .drawWithCache {
                val path = sketchWavyLinePath(
                    height = size.height,
                    centerX = size.width / 2f,
                    amplitude = amplitude,
                    wavelength = wavelength,
                    noiseAmount = noiseAmount,
                    seed = seed,
                )
                val stroke = Stroke(width = thickness.toPx(), cap = StrokeCap.Round)
                onDrawBehind {
                    drawPath(path = path, color = color, style = stroke)
                }
            },
    )
}

/**
 * Outlines the content as a hand-sketched rectangle.
 *
 * The stroke is inset so that the whole wobble stays inside the layout bounds,
 * which costs `roughness * (1 + tremor) + thickness / 2` of padding on every edge.
 */
fun Modifier.sketchBorder(
    seed: Int,
    color: Color,
    thickness: Dp = 2.dp,
    roughness: Dp = DefaultRoughness,
    tremor: Dp = DefaultTremor,
    sweepWavelength: Dp = DefaultSweepWavelength,
    tremorWavelength: Dp = DefaultTremorWavelength,
    cornerRadius: Dp = 0.dp,
): Modifier = drawWithCache {
    val ratio = swingCapRatio(size.width, size.height, roughness, tremor)
    val effectiveRoughness = roughness * ratio
    val effectiveTremor = tremor * ratio
    val inset = (effectiveRoughness + effectiveTremor).toPx() + thickness.toPx() / 2f
    val width = size.width - inset * 2f
    val height = size.height - inset * 2f
    if (width <= 0f || height <= 0f) return@drawWithCache onDrawBehind {}

    val path = sketchRoundRectPath(
        width = width,
        height = height,
        cornerRadius = cornerRadius,
        roughness = effectiveRoughness,
        tremor = effectiveTremor,
        sweepWavelength = sweepWavelength,
        tremorWavelength = tremorWavelength,
        seed = seed,
    )
    path.translate(Offset(inset, inset))
    val stroke = Stroke(
        width = thickness.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )
    onDrawBehind {
        drawPath(path = path, color = color, style = stroke)
    }
}

/**
 * The hand-sketched rectangle as a [Shape], for `Modifier.clip`, `Modifier.background`
 * or any `shape` parameter.
 *
 * The outline is inset by `roughness + tremor` so the whole wobble stays within
 * the bounds. [Modifier.sketchBorder] insets a further half of its stroke width to keep
 * the line itself inside, so pairing the two leaves that half-width between them.
 *
 * On a box too small to hold the requested swing, both octaves shrink together rather
 * than letting the outline fold onto itself.
 */
@Immutable
data class SketchShape(
    val seed: Int,
    val roughness: Dp = DefaultRoughness,
    val tremor: Dp = DefaultTremor,
    val sweepWavelength: Dp = DefaultSweepWavelength,
    val tremorWavelength: Dp = DefaultTremorWavelength,
    val cornerRadius: Dp = 0.dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = with(density) {
        val ratio = swingCapRatio(size.width, size.height, roughness, tremor)
        val effectiveRoughness = roughness * ratio
        val effectiveTremor = tremor * ratio
        val inset = (effectiveRoughness + effectiveTremor).toPx()
        val width = size.width - inset * 2f
        val height = size.height - inset * 2f
        if (width <= 0f || height <= 0f) return Outline.Rectangle(size.toRect())

        val path = sketchRoundRectPath(
            width = width,
            height = height,
            cornerRadius = cornerRadius,
            roughness = effectiveRoughness,
            tremor = effectiveTremor,
            sweepWavelength = sweepWavelength,
            tremorWavelength = tremorWavelength,
            seed = seed,
        )
        path.translate(Offset(inset, inset))
        Outline.Generic(path)
    }
}

@Preview
@Composable
private fun SketchDividerPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(4) { index ->
                    SketchDivider(seed = index)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SketchDivider(seed = 10, modifier = Modifier.width(80.dp))
                    SketchDivider(seed = 11, modifier = Modifier.width(160.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun SketchDividerTastePreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            DividerTasteGrid()
        }
    }
}

@Composable
private fun DividerTasteGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DividerTasteRow(roughness = 0.5.dp)
        DividerTasteRow(roughness = 1.dp)
        DividerTasteRow(roughness = 3.dp)
    }
}

@Composable
private fun DividerTasteRow(roughness: Dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        TREMOR_STEPS.forEach { tremor ->
            LabelledSample(label = "${roughness.value.toInt()}dp / $tremor") {
                SketchDivider(
                    seed = 7,
                    modifier = Modifier.width(150.dp),
                    roughness = roughness,
                    tremor = tremor,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SketchDividerFinenessPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            FinenessSamples()
        }
    }
}

@Composable
private fun FinenessSamples() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(80.dp, 42.dp, 20.dp, 10.dp).forEach { wavelength ->
            LabelledSample(label = "${wavelength.value.toInt()}dp") {
                SketchDivider(
                    seed = 4,
                    modifier = Modifier.width(320.dp),
                    tremor = 1.dp,
                    tremorWavelength = wavelength,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SketchDividerWeightPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            DividerWeightSamples()
        }
    }
}

@Composable
private fun DividerWeightSamples() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(0.75.dp, 1.5.dp, 3.dp, 6.dp).forEach { thickness ->
            LabelledSample(label = "${thickness.value}dp") {
                SketchDivider(
                    seed = 12,
                    modifier = Modifier.width(320.dp),
                    thickness = thickness,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SketchWavyLinePreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                WavyNoiseSamples()
                WavyWavelengthSamples()
            }
        }
    }
}

@Composable
private fun WavyNoiseSamples() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        listOf(0f, 0.4f, 0.8f, 1.2f).forEach { amount ->
            LabelledSample(label = "n=$amount") {
                SketchWavyLine(
                    seed = 3,
                    modifier = Modifier.height(60.dp),
                    thickness = 2.dp,
                    noiseAmount = amount,
                )
            }
        }
    }
}

@Composable
private fun WavyWavelengthSamples() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        listOf(6.dp, 10.dp, 16.dp, 24.dp).forEach { wavelength ->
            LabelledSample(label = "${wavelength.value.toInt()}dp") {
                SketchWavyLine(
                    seed = 3,
                    modifier = Modifier.height(60.dp),
                    thickness = 2.dp,
                    wavelength = wavelength,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SketchBorderCornerRadiusPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            CornerRadiusSamples()
        }
    }
}

@Composable
private fun CornerRadiusSamples() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(0.dp, 8.dp, 16.dp, 28.dp).forEachIndexed { index, radius ->
            LabelledSample(label = "r=${radius.value.toInt()}") {
                Box(
                    Modifier
                        .size(90.dp, 64.dp)
                        .sketchBorder(
                            seed = 20 + index,
                            color = MaterialTheme.colorScheme.outline,
                            cornerRadius = radius,
                        ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun SketchBorderTastePreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            BorderTasteGrid()
        }
    }
}

@Composable
private fun BorderTasteGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BorderTasteRow(roughness = 0.5.dp)
        BorderTasteRow(roughness = 1.dp)
        BorderTasteRow(roughness = 3.dp)
    }
}

@Composable
private fun BorderTasteRow(roughness: Dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TREMOR_STEPS.forEach { tremor ->
            LabelledSample(label = "${roughness.value.toInt()}dp / $tremor") {
                Box(
                    Modifier
                        .size(132.dp, 84.dp)
                        .sketchBorder(
                            seed = 9,
                            color = MaterialTheme.colorScheme.outline,
                            roughness = roughness,
                            tremor = tremor,
                            cornerRadius = 10.dp,
                        ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun SketchShapePreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LabelledSample(label = "clip") {
                    Box(
                        Modifier
                            .size(90.dp, 64.dp)
                            .clip(SketchShape(seed = 50, cornerRadius = 12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    )
                }
                LabelledSample(label = "clip + border") {
                    Box(
                        Modifier
                            .size(90.dp, 64.dp)
                            .clip(SketchShape(seed = 51, cornerRadius = 12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .sketchBorder(
                                seed = 51,
                                color = MaterialTheme.colorScheme.outline,
                                cornerRadius = 12.dp,
                            ),
                    )
                }
                LabelledSample(label = "Surface") {
                    Surface(
                        modifier = Modifier.size(90.dp, 64.dp),
                        shape = SketchShape(seed = 52, cornerRadius = 20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        content = {},
                    )
                }
                LabelledSample(label = "tremor=1") {
                    Box(
                        Modifier
                            .size(90.dp, 64.dp)
                            .clip(SketchShape(seed = 53, tremor = 1.dp, cornerRadius = 12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SketchCardPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            Column(
                Modifier
                    .width(260.dp)
                    .sketchBorder(
                        seed = 40,
                        color = MaterialTheme.colorScheme.outline,
                        cornerRadius = 16.dp,
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "スタンプラリー",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                SketchDivider(seed = 41, thickness = 1.5.dp)
                Text(
                    text = "会場をめぐってスタンプを集めましょう",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        content()
    }
}

@Composable
private fun LabelledSample(label: String, content: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        content()
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
