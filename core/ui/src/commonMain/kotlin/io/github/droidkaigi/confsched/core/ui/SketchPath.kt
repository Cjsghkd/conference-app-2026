package io.github.droidkaigi.confsched.core.ui

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private val SweepWavelength = 300.dp

// Anchors carry the tremor, so they have to be dense enough to resolve it: roughly four
// to a wavelength. The ceiling keeps a long wavelength from thinning them out until the
// broad sweep itself turns polygonal.
private const val ANCHORS_PER_WAVE = 4f
private val MaxAnchorSpacing = 12.dp

private fun anchorSpacingFor(tremorWavelength: Dp): Dp =
    minOf(tremorWavelength / ANCHORS_PER_WAVE, MaxAnchorSpacing)

// Decorrelates the tremor octave from the sweep; any value that is not a
// multiple of the noise lattice works.
private const val SECOND_OCTAVE_OFFSET = 17f
private const val SECOND_OCTAVE_SCALE = 2.3f

private const val PHASE_RANGE = 50f
private const val LINE_LATTICE_SIZE = 256
private const val QUARTER_TURN = PI.toFloat() / 2f

// A sweep wavelength longer than the perimeter would leave one lattice cell per edge,
// which offsets each edge by a near-constant amount and reads as a trapezoid rather
// than a drawn rectangle. Six cells put roughly one and a half undulations on a side.
private const val MIN_SWEEP_CELLS = 6

// How far a Catmull-Rom tangent may reach, as a fraction of the segment it belongs to.
private const val TANGENT_CLAMP = 0.33f

/**
 * A horizontal line of [width], wobbling around [centerY].
 *
 * [roughness] is the half-span of the broad sweep and [tremor] that of the finer
 * wobble layered on top of it, both in dp, so the line reaches `roughness + tremor`
 * away from [centerY] at most.
 */
internal fun Density.sketchLinePath(
    width: Float,
    centerY: Float,
    roughness: Dp,
    tremor: Dp,
    tremorWavelength: Dp,
    seed: Int,
): Path {
    val sweepAmplitude = roughness.toPx()
    val tremorAmplitude = tremor.toPx()
    val sweepWavelengthPx = SweepWavelength.toPx()
    val tremorWavelengthPx = tremorWavelength.toPx()

    val random = Random(seed)
    val noise = ValueNoise(random, LINE_LATTICE_SIZE)
    val phase = random.nextFloat() * PHASE_RANGE

    val segments = max(2, ceil(width / anchorSpacingFor(tremorWavelength).toPx()).toInt())
    val xs = FloatArray(segments + 1) { width * it / segments }
    val ys = FloatArray(segments + 1) { noise.at(phase + xs[it] / sweepWavelengthPx) }
    normalizeToPeak(ys, sweepAmplitude)
    // The tremor octave is added raw rather than normalized: normalizing it would
    // scale every fine wave down to the single largest one, flattening the effect.
    val tremorPhase = phase * SECOND_OCTAVE_SCALE + SECOND_OCTAVE_OFFSET
    for (index in ys.indices) {
        ys[index] += centerY + noise.at(tremorPhase + xs[index] / tremorWavelengthPx) * tremorAmplitude
    }

    return Path().apply {
        moveTo(xs[0], ys[0])
        val controls = FloatArray(4)
        for (index in 0 until segments) {
            val previous = max(index - 1, 0)
            val next = min(index + 2, segments)
            controlPointsFor(
                p0x = xs[previous], p0y = ys[previous],
                p1x = xs[index], p1y = ys[index],
                p2x = xs[index + 1], p2y = ys[index + 1],
                p3x = xs[next], p3y = ys[next],
                out = controls,
            )
            cubicTo(controls[0], controls[1], controls[2], controls[3], xs[index + 1], ys[index + 1])
        }
    }
}

/**
 * A closed round rect of [width] by [height], wobbling around its outline.
 *
 * Each anchor is displaced along the outward normal by the same two octaves
 * [sketchLinePath] uses, so the outline reaches `roughness + tremor` beyond
 * the nominal rectangle at most. Pass a [roughness] already scaled by
 * [swingCapRatio] and inset by the same amount, so the swing stays inside the
 * bounds and cannot fold the outline onto itself.
 */
internal fun Density.sketchRoundRectPath(
    width: Float,
    height: Float,
    cornerRadius: Dp,
    roughness: Dp,
    tremor: Dp,
    tremorWavelength: Dp,
    seed: Int,
): Path {
    val sweepAmplitude = roughness.toPx()
    val tremorAmplitude = tremor.toPx()
    val radius = cornerRadius.toPx().coerceIn(0f, min(width, height) / 2f)
    val straightWidth = width - radius * 2f
    val straightHeight = height - radius * 2f
    val arcLength = QUARTER_TURN * radius
    val lengths = floatArrayOf(
        straightWidth,
        arcLength,
        straightHeight,
        arcLength,
        straightWidth,
        arcLength,
        straightHeight,
        arcLength,
    )
    val perimeter = lengths.sum()

    val distances = anchorDistances(lengths, anchorSpacingFor(tremorWavelength).toPx())
    val random = Random(seed)
    val sweepNoise = ValueNoise(random, cellsFor(perimeter, SweepWavelength.toPx()))
    val tremorNoise = ValueNoise(random, cellsFor(perimeter, tremorWavelength.toPx()))
    val offsets = FloatArray(distances.size) { sweepNoise.atCyclic(distances[it] / perimeter) }
    normalizeToPeak(offsets, sweepAmplitude)
    for (index in offsets.indices) {
        offsets[index] += tremorNoise.atCyclic(distances[index] / perimeter) * tremorAmplitude
    }

    val xs = FloatArray(distances.size)
    val ys = FloatArray(distances.size)
    for (index in distances.indices) {
        val point = outlinePoint(distances[index], width, height, radius, straightWidth, straightHeight)
        xs[index] = point.x + point.normalX * offsets[index]
        ys[index] = point.y + point.normalY * offsets[index]
    }

    val count = xs.size
    return Path().apply {
        moveTo(xs[0], ys[0])
        val controls = FloatArray(4)
        for (index in 0 until count) {
            val previous = (index - 1).mod(count)
            val current = index
            val next = (index + 1).mod(count)
            val following = (index + 2).mod(count)
            // A duplicated corner anchor would emit a zero-length curve, which a round
            // join renders as a visible dot. Skipping it keeps the corner sharp, since
            // the duplicate still steers the tangents of the segments either side.
            if (xs[next] == xs[current] && ys[next] == ys[current]) continue
            controlPointsFor(
                p0x = xs[previous], p0y = ys[previous],
                p1x = xs[current], p1y = ys[current],
                p2x = xs[next], p2y = ys[next],
                p3x = xs[following], p3y = ys[following],
                out = controls,
            )
            cubicTo(controls[0], controls[1], controls[2], controls[3], xs[next], ys[next])
        }
        close()
    }
}

/**
 * How much the requested swing has to shrink to fit a box of [width] by [height].
 *
 * Anchors move along the outward normal, so a swing wider than a quarter of the shorter
 * side carries opposing edges through each other and folds the outline into a ribbon.
 * Both octaves are scaled by this one ratio, which caps the swing while keeping their
 * proportion, so a shape small enough to reach the cap draws calmer rather than
 * differently.
 *
 * The ratio is taken against the full box, not the inset outline: the inset depends on
 * the capped swing, so measuring the cap against the inset would be circular.
 */
internal fun Density.swingCapRatio(width: Float, height: Float, roughness: Dp, tremor: Dp): Float {
    val requested = roughness.toPx() + tremor.toPx()
    if (requested <= 0f) return 1f
    return min(1f, min(width, height) / 4f / requested)
}

/**
 * Control points of the cubic that reproduces the centripetal Catmull-Rom segment from
 * `p1` to `p2`, written into [out] as `c1x, c1y, c2x, c2y`.
 *
 * Centripetal parameterization (α = 0.5) weights each tangent by the square root of the
 * distance to its neighbour, which keeps the curve from looping back on itself where the
 * anchors are unevenly spaced — the uniform form overshoots badly around a short segment
 * next to a long one. A duplicated anchor collapses one weight to zero, so the tangent
 * there degenerates to the anchor itself, which is what leaves a corner sharp.
 */
private fun controlPointsFor(
    p0x: Float,
    p0y: Float,
    p1x: Float,
    p1y: Float,
    p2x: Float,
    p2y: Float,
    p3x: Float,
    p3y: Float,
    out: FloatArray,
) {
    val d1 = knotSpacing(p0x, p0y, p1x, p1y)
    val d2 = knotSpacing(p1x, p1y, p2x, p2y)
    val d3 = knotSpacing(p2x, p2y, p3x, p3y)

    var c1x = p1x
    var c1y = p1y
    if (d1 > 0f && d2 > 0f) {
        val scale = 3f * d1 * (d1 + d2)
        val weight = 2f * d1 * d1 + 3f * d1 * d2 + d2 * d2
        c1x = (d1 * d1 * p2x - d2 * d2 * p0x + weight * p1x) / scale
        c1y = (d1 * d1 * p2y - d2 * d2 * p0y + weight * p1y) / scale
    }

    var c2x = p2x
    var c2y = p2y
    if (d3 > 0f && d2 > 0f) {
        val scale = 3f * d3 * (d3 + d2)
        val weight = 2f * d3 * d3 + 3f * d3 * d2 + d2 * d2
        c2x = (d3 * d3 * p1x - d2 * d2 * p3x + weight * p2x) / scale
        c2y = (d3 * d3 * p1y - d2 * d2 * p3y + weight * p2y) / scale
    }

    val reach = hypot(p2x - p1x, p2y - p1y) * TANGENT_CLAMP
    clampToReach(p1x, p1y, c1x, c1y, reach, out, 0)
    clampToReach(p2x, p2y, c2x, c2y, reach, out, 2)
}

/** Knot spacing under centripetal parameterization: the distance raised to α = 0.5. */
private fun knotSpacing(ax: Float, ay: Float, bx: Float, by: Float): Float =
    sqrt(hypot(bx - ax, by - ay))

/**
 * Pulls a control point back towards its anchor until it is at most [reach] away.
 *
 * Even under centripetal weighting a tangent can run far past the segment it belongs to
 * when the noise turns sharply; capping it in proportion to the segment length keeps the
 * curve inside the band the anchors describe.
 */
private fun clampToReach(
    anchorX: Float,
    anchorY: Float,
    controlX: Float,
    controlY: Float,
    reach: Float,
    out: FloatArray,
    offset: Int,
) {
    val dx = controlX - anchorX
    val dy = controlY - anchorY
    val distance = hypot(dx, dy)
    if (distance <= reach || distance == 0f) {
        out[offset] = controlX
        out[offset + 1] = controlY
        return
    }
    val scale = reach / distance
    out[offset] = anchorX + dx * scale
    out[offset + 1] = anchorY + dy * scale
}

/**
 * Anchor positions along the perimeter, pinned to every segment boundary.
 *
 * A zero-length arc contributes a duplicated anchor, and that duplicate is what
 * lets a sharp corner survive the spline that is otherwise smooth everywhere.
 *
 * The result is rotated so the path starts, and therefore closes, away from any corner.
 */
private fun anchorDistances(lengths: FloatArray, anchorSpacing: Float): FloatArray {
    val distances = ArrayList<Float>(lengths.size * 4)
    var start = 0f
    lengths.forEachIndexed { index, length ->
        if (length <= 0f) {
            distances += start
        } else {
            val minimumSteps = if (index % 2 == 1) 2 else 1
            val steps = max(minimumSteps, (length / anchorSpacing).roundToInt())
            for (step in 0 until steps) {
                distances += start + length * step / steps
            }
        }
        start += length
    }

    // Start midway along the top edge. The closing seam of the path joins its two ends
    // with a smooth tangent, which is wrong at a corner: a corner is exactly where the
    // tangent has to break, and a seam placed there rounds it off.
    val seam = lengths[0] / 2f
    val rotated = ArrayList<Float>(distances.size)
    distances.filterTo(rotated) { it >= seam }
    distances.filterTo(rotated) { it < seam }
    return rotated.toFloatArray()
}

private fun outlinePoint(
    distance: Float,
    width: Float,
    height: Float,
    radius: Float,
    straightWidth: Float,
    straightHeight: Float,
): OutlinePoint {
    val arcLength = QUARTER_TURN * radius
    // A distance of exactly the perimeter is the start point. Without this it would fall
    // through to the last arc and come back with that arc's normal, so the anchor would
    // be pushed along a different direction than the start point it coincides with.
    val perimeter = (straightWidth + straightHeight) * 2f + arcLength * 4f
    var remaining = if (distance >= perimeter) distance - perimeter else distance

    if (remaining < straightWidth) return OutlinePoint(radius + remaining, 0f, 0f, -1f)
    remaining -= straightWidth
    if (remaining < arcLength) {
        return arcPoint(remaining / arcLength, -QUARTER_TURN, width - radius, radius, radius)
    }
    remaining -= arcLength
    if (remaining < straightHeight) return OutlinePoint(width, radius + remaining, 1f, 0f)
    remaining -= straightHeight
    if (remaining < arcLength) {
        return arcPoint(remaining / arcLength, 0f, width - radius, height - radius, radius)
    }
    remaining -= arcLength
    if (remaining < straightWidth) return OutlinePoint(width - radius - remaining, height, 0f, 1f)
    remaining -= straightWidth
    if (remaining < arcLength) {
        return arcPoint(remaining / arcLength, QUARTER_TURN, radius, height - radius, radius)
    }
    remaining -= arcLength
    if (remaining < straightHeight) return OutlinePoint(0f, height - radius - remaining, -1f, 0f)
    remaining -= straightHeight

    val progress = if (arcLength > 0f) remaining / arcLength else 0f
    return arcPoint(progress, PI.toFloat(), radius, radius, radius)
}

private fun arcPoint(
    progress: Float,
    startAngle: Float,
    centerX: Float,
    centerY: Float,
    radius: Float,
): OutlinePoint {
    val angle = startAngle + progress * QUARTER_TURN
    val normalX = cos(angle)
    val normalY = sin(angle)
    return OutlinePoint(
        x = centerX + normalX * radius,
        y = centerY + normalY * radius,
        normalX = normalX,
        normalY = normalY,
    )
}

private class OutlinePoint(
    val x: Float,
    val y: Float,
    val normalX: Float,
    val normalY: Float,
)

/**
 * Rescales [values] so their peak-to-peak span is exactly `2 * amplitude`.
 *
 * Without this the visible waviness would depend on how many noise lattice
 * cells happen to fit in the drawn length.
 */
private fun normalizeToPeak(values: FloatArray, amplitude: Float) {
    val lowest = values.min()
    val highest = values.max()
    val halfRange = (highest - lowest) / 2f
    if (halfRange == 0f) {
        values.fill(0f)
        return
    }
    val midpoint = (highest + lowest) / 2f
    for (index in values.indices) {
        values[index] = (values[index] - midpoint) / halfRange * amplitude
    }
}

private fun cellsFor(perimeter: Float, wavelength: Float): Int =
    max(MIN_SWEEP_CELLS, (perimeter / wavelength).roundToInt())

private class ValueNoise(random: Random, size: Int) {
    private val lattice = FloatArray(size) { random.nextFloat() * 2f - 1f }

    fun at(x: Float): Float {
        val index = floor(x).toInt()
        return interpolate(index, x - index)
    }

    /** Samples a closed loop: [t] in `[0, 1)` wraps back onto the first cell. */
    fun atCyclic(t: Float): Float {
        val x = t * lattice.size
        val index = floor(x).toInt()
        return interpolate(index, x - index)
    }

    private fun interpolate(index: Int, fraction: Float): Float {
        val start = lattice[index.mod(lattice.size)]
        val end = lattice[(index + 1).mod(lattice.size)]
        return start + (end - start) * fraction * fraction * (3f - 2f * fraction)
    }
}
