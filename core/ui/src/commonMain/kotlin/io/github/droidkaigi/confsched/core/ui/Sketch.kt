package io.github.droidkaigi.confsched.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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

/**
 * How far apart the broad sweep turns.
 *
 * A closed outline wraps its noise lattice onto the perimeter and so rounds the wavelength
 * to a whole number of cells. At this figure the components in the design spec land on four
 * to six cells, which is where the parameter starts governing the result instead of being
 * overridden by the floor that keeps a rectangle from reading as a skewed quadrilateral.
 */
private val DefaultSweepWavelength = 140.dp

/**
 * A wavelength divides, so zero would put the noise lookup at infinity, and a
 * negative amplitude reverses the swing and can drive a computed height below zero,
 * which the layout rejects at draw time rather than at the call site.
 */
private fun requireWobble(roughness: Dp, tremor: Dp, sweepWavelength: Dp, tremorWavelength: Dp) {
    require(roughness >= 0.dp) { "roughness must not be negative, was $roughness" }
    require(tremor >= 0.dp) { "tremor must not be negative, was $tremor" }
    require(sweepWavelength > 0.dp) { "sweepWavelength must be positive, was $sweepWavelength" }
    require(tremorWavelength > 0.dp) { "tremorWavelength must be positive, was $tremorWavelength" }
}
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
fun SketchHorizontalDivider(
    seed: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline,
    thickness: Dp = 2.dp,
    roughness: Dp = DefaultRoughness,
    tremor: Dp = DefaultTremor,
    sweepWavelength: Dp = DefaultSweepWavelength,
    tremorWavelength: Dp = DefaultTremorWavelength,
) {
    requireWobble(roughness, tremor, sweepWavelength, tremorWavelength)
    require(thickness >= 0.dp) { "thickness must not be negative, was $thickness" }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness + (roughness + tremor) * 2)
            .drawWithCache {
                val path = sketchHorizontalLinePath(
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
 * A vertical rule drawn as a single hand-sketched stroke.
 *
 * The line [SketchHorizontalDivider] draws, running down instead of across: a given
 * [seed] describes the same wobble either way.
 */
@Composable
fun SketchVerticalDivider(
    seed: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline,
    thickness: Dp = 2.dp,
    roughness: Dp = DefaultRoughness,
    tremor: Dp = DefaultTremor,
    sweepWavelength: Dp = DefaultSweepWavelength,
    tremorWavelength: Dp = DefaultTremorWavelength,
) {
    requireWobble(roughness, tremor, sweepWavelength, tremorWavelength)
    require(thickness >= 0.dp) { "thickness must not be negative, was $thickness" }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(thickness + (roughness + tremor) * 2)
            .drawWithCache {
                val path = sketchVerticalLinePath(
                    height = size.height,
                    centerX = size.width / 2f,
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
 * The caller sets the height; the width follows from the reach of the widest crest.
 * [noiseAmount] decides how mechanical the ripple looks, from an even sine at `0`
 * to visibly uneven crests at `1`, and it swells [amplitude] as well as shrinking it,
 * so the line needs `amplitude * (1 + noiseAmount)` either side of centre.
 */
@Composable
fun SketchVerticalWavyLine(
    seed: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline,
    thickness: Dp = 1.dp,
    amplitude: Dp = 3.dp,
    wavelength: Dp = 10.dp,
    noiseAmount: Float = 0.8f,
) {
    require(thickness >= 0.dp) { "thickness must not be negative, was $thickness" }
    require(amplitude >= 0.dp) { "amplitude must not be negative, was $amplitude" }
    require(wavelength > 0.dp) { "wavelength must be positive, was $wavelength" }
    require(noiseAmount >= 0f) { "noiseAmount must not be negative, was $noiseAmount" }
    Box(
        modifier = modifier
            .width(amplitude * (1f + noiseAmount) * 2 + thickness)
            .drawWithCache {
                val path = sketchVerticalWavyLinePath(
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
): Modifier {
    requireWobble(roughness, tremor, sweepWavelength, tremorWavelength)
    require(thickness >= 0.dp) { "thickness must not be negative, was $thickness" }
    require(cornerRadius >= 0.dp) { "cornerRadius must not be negative, was $cornerRadius" }
    return drawWithCache {
        val ratio = swingCapRatio(size.width, size.height, roughness, tremor, thickness)
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
}

/**
 * The hand-sketched rectangle as a [Shape], for `Modifier.clip`, `Modifier.background`
 * or any `shape` parameter.
 *
 * The outline is inset by `roughness + tremor` so the whole wobble stays within the
 * bounds. Give [borderThickness] the `thickness` of the [Modifier.sketchBorder] this shape
 * is paired with and the two land on the same vertices, so a region clipped to the shape
 * meets the line down its centre instead of spilling a stroke's width past it. Left at
 * zero the outline sits where an unaccompanied fill wants it.
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
    val borderThickness: Dp = 0.dp,
) : Shape {
    init {
        requireWobble(roughness, tremor, sweepWavelength, tremorWavelength)
        require(cornerRadius >= 0.dp) { "cornerRadius must not be negative, was $cornerRadius" }
        require(borderThickness >= 0.dp) { "borderThickness must not be negative, was $borderThickness" }
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = with(density) {
        val ratio = swingCapRatio(size.width, size.height, roughness, tremor, borderThickness)
        val effectiveRoughness = roughness * ratio
        val effectiveTremor = tremor * ratio
        val inset = (effectiveRoughness + effectiveTremor).toPx() + borderThickness.toPx() / 2f
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
private fun SketchHorizontalDividerPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(4) { index ->
                    SketchHorizontalDivider(seed = index)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SketchHorizontalDivider(seed = 10, modifier = Modifier.width(80.dp))
                    SketchHorizontalDivider(seed = 11, modifier = Modifier.width(160.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun SketchHorizontalDividerTastePreview(
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
                SketchHorizontalDivider(
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
private fun SketchHorizontalDividerFinenessPreview(
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
                SketchHorizontalDivider(
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
private fun SketchHorizontalDividerWeightPreview(
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
                SketchHorizontalDivider(
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
private fun SketchVerticalWavyLinePreview(
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
                SketchVerticalWavyLine(
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
                SketchVerticalWavyLine(
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
private fun SketchSegmentedPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SegmentedSample(selectedFirst = true)
                SegmentedSample(selectedFirst = false)
            }
        }
    }
}

@Composable
private fun SegmentedSample(selectedFirst: Boolean) {
    val thickness = 2.dp
    val roughness = 0.4.dp
    val tremor = 0.15.dp
    val cornerRadius = 16.dp
    Box(Modifier.size(208.dp, 40.dp)) {
        // Clipping the fill out of the outline the border strokes is what keeps its edge
        // under the middle of the line instead of beside it.
        Box(
            Modifier
                .matchParentSize()
                .clip(
                    SketchShape(
                        seed = 900,
                        roughness = roughness,
                        tremor = tremor,
                        cornerRadius = cornerRadius,
                        borderThickness = thickness,
                    ),
                ),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight()
                    .align(if (selectedFirst) Alignment.CenterStart else Alignment.CenterEnd)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            )
            // Inside the clip, so the rule stops at the outline instead of running the
            // full height and poking out past the top and bottom of the border.
            SketchVerticalDivider(
                seed = 902,
                modifier = Modifier.align(Alignment.Center),
                thickness = 1.dp,
                roughness = 0.3.dp,
            )
        }
        Box(
            Modifier
                .matchParentSize()
                .sketchBorder(
                    seed = 900,
                    color = MaterialTheme.colorScheme.outline,
                    thickness = thickness,
                    roughness = roughness,
                    tremor = tremor,
                    cornerRadius = cornerRadius,
                ),
        )
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
                SketchHorizontalDivider(seed = 41, thickness = 1.5.dp)
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
