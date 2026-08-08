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

// Anchors carry the tremor, so they have to be dense enough to resolve it: roughly four
// to a wavelength. The ceiling keeps a long wavelength from thinning them out until the
// broad sweep itself turns polygonal.
private const val ANCHORS_PER_WAVE = 4f
private val MaxAnchorSpacing = 12.dp

private fun anchorSpacingFor(tremorWavelength: Dp): Dp =
    minOf(tremorWavelength / ANCHORS_PER_WAVE, MaxAnchorSpacing)

// Offsets the tremor octave onto its own sequence, so the two bands stay uncorrelated.
private const val TREMOR_SEED_OFFSET = 100

// Constants of the specified hash: odd multipliers with well-spread bits, and the scale
// that maps its top 24 bits onto [0, 1).
private const val SEED_MULTIPLIER = 374761393
private const val INDEX_MULTIPLIER = 668265263
private const val MIX_MULTIPLIER = 1274126177
private const val HASH_SCALE = 16777216f
private const val QUARTER_TURN = PI.toFloat() / 2f
private const val TWO_PI = PI.toFloat() * 2f

// Anchors per ripple of the wavy line: enough for a Catmull-Rom fit to read as a
// sine rather than a zigzag.
private const val POINTS_PER_WAVE = 6

// Below about four cells the sweep offsets whole edges by a near-constant amount and the
// rectangle tilts into a skewed quadrilateral. Four is where that stops showing at a
// roughness well past anything in use; two is unmistakable.
private const val MIN_SWEEP_CELLS = 4

// The tremor lattice only has to stay a lattice. At one cell the interpolation reads the
// same value at both ends and the octave flattens to a constant offset.
private const val MIN_TREMOR_CELLS = 2

// How far a Catmull-Rom tangent may reach, as a fraction of the segment it belongs to.
// A circular arc needs a shade over a third of its chord however finely it is subdivided,
// so a clamp at or below 1/3 facets every corner it passes through; a small radius drawn
// from few anchors then reads as a polygon. Both values sit above that, leaving the clamp
// to catch only the overshoot a sharp turn in the noise produces.
private const val OUTLINE_TANGENT_CLAMP = 0.36f
private const val WAVY_TANGENT_CLAMP = 0.4f

/**
 * A horizontal line of [width], wobbling around [centerY].
 *
 * [roughness] is the half-span of the broad sweep and [tremor] that of the finer
 * wobble layered on top of it, both in dp, so the line reaches `roughness + tremor`
 * away from [centerY] at most.
 */
internal fun Density.sketchHorizontalLinePath(
    width: Float,
    centerY: Float,
    roughness: Dp,
    tremor: Dp,
    sweepWavelength: Dp,
    tremorWavelength: Dp,
    seed: Int,
): Path {
    val positions = sketchLinePositions(width, tremorWavelength)
    val offsets = sketchLineOffsets(
        positions = positions,
        center = centerY,
        roughness = roughness,
        tremor = tremor,
        sweepWavelength = sweepWavelength,
        tremorWavelength = tremorWavelength,
        seed = seed,
    )
    return openCurveThrough(positions, offsets, OUTLINE_TANGENT_CLAMP)
}

/**
 * A vertical line of [height], wobbling around [centerX].
 *
 * The stroke is the one [sketchHorizontalLinePath] draws laid on the other axis, so a
 * given [seed] describes the same wobble whichever direction the line runs.
 */
internal fun Density.sketchVerticalLinePath(
    height: Float,
    centerX: Float,
    roughness: Dp,
    tremor: Dp,
    sweepWavelength: Dp,
    tremorWavelength: Dp,
    seed: Int,
): Path {
    val positions = sketchLinePositions(height, tremorWavelength)
    val offsets = sketchLineOffsets(
        positions = positions,
        center = centerX,
        roughness = roughness,
        tremor = tremor,
        sweepWavelength = sweepWavelength,
        tremorWavelength = tremorWavelength,
        seed = seed,
    )
    return openCurveThrough(offsets, positions, OUTLINE_TANGENT_CLAMP)
}

/**
 * A vertical wavy line of [height], rippling either side of [centerX].
 *
 * Unlike the other two, this one is a sine wave rather than pure noise:
 * [wavelength] sets the pitch of the ripple and [amplitude] its reach.
 * [noiseAmount] modulates that reach point by point, from a mechanical wave
 * at `0` to one whose crests vary widely at `1`.
 */
internal fun Density.sketchVerticalWavyLinePath(
    height: Float,
    centerX: Float,
    amplitude: Dp,
    wavelength: Dp,
    noiseAmount: Float,
    seed: Int,
): Path {
    val reach = amplitude.toPx()
    val pitch = wavelength.toPx()
    val steps = max(2, (height / pitch * POINTS_PER_WAVE).roundToInt())
    val ys = FloatArray(steps + 1) { height * it / steps }
    val xs = FloatArray(steps + 1) { index ->
        val turns = ys[index] / pitch
        val swell = 1f + coherentNoise(seed, ys[index], pitch) * noiseAmount
        centerX + sin(turns * TWO_PI) * reach * swell
    }

    return openCurveThrough(xs, ys, WAVY_TANGENT_CLAMP)
}

/**
 * A closed round rect of [width] by [height], wobbling around its outline.
 *
 * Each anchor is displaced along the outward normal by the same two octaves
 * [sketchHorizontalLinePath] uses, so the outline reaches `roughness + tremor` beyond
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
    sweepWavelength: Dp,
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

    val sweepCells = cellsFor(perimeter, sweepWavelength.toPx(), MIN_SWEEP_CELLS)
    val tremorCells = cellsFor(perimeter, tremorWavelength.toPx(), MIN_TREMOR_CELLS)
    // Anchors have to resolve the lattice the closed path ends up with, not the one the
    // nominal wavelength describes. Wrapping rounds the lattice to a whole number of cells,
    // and on a short perimeter that lands far finer than the wavelength asked for; spacing
    // the anchors by the nominal figure would drop the tremor between them.
    val anchorSpacing = min(perimeter / tremorCells / ANCHORS_PER_WAVE, MaxAnchorSpacing.toPx())
    val distances = anchorDistances(lengths, anchorSpacing)
    val offsets = FloatArray(distances.size) { coherentNoiseCyclic(seed, distances[it] / perimeter, sweepCells) }
    normalizeToPeak(offsets, sweepAmplitude)
    for (index in offsets.indices) {
        val t = distances[index] / perimeter
        offsets[index] += coherentNoiseCyclic(seed + TREMOR_SEED_OFFSET, t, tremorCells) * tremorAmplitude
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
                tangentClamp = OUTLINE_TANGENT_CLAMP,
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
 * side of the outline carries opposing edges through each other and folds it into a ribbon.
 * Both octaves are scaled by this one ratio, which caps the swing while keeping their
 * proportion, so a shape small enough to reach the cap draws calmer rather than
 * differently.
 *
 * The outline is drawn inside a box already inset by the swing itself plus half of
 * [strokeWidth], so the bound is solved rather than read off the box: with `a` the swing,
 * the inner side measures `min(width, height) - 2a - strokeWidth`, and holding `a` within a
 * quarter of that gives `a <= (min(width, height) - strokeWidth) / 6`. Measuring against the
 * full box instead lets a large swing shrink the outline faster than the cap tightens, and
 * the shape folds anyway.
 */
internal fun Density.swingCapRatio(
    width: Float,
    height: Float,
    roughness: Dp,
    tremor: Dp,
    strokeWidth: Dp,
): Float {
    val requested = roughness.toPx() + tremor.toPx()
    if (requested <= 0f) return 1f
    val cap = (min(width, height) - strokeWidth.toPx()) / 6f
    if (cap <= 0f) return 0f
    return min(1f, cap / requested)
}

/** Anchor positions spread evenly along a line of [length], dense enough to carry the tremor. */
private fun Density.sketchLinePositions(length: Float, tremorWavelength: Dp): FloatArray {
    val segments = max(2, ceil(length / anchorSpacingFor(tremorWavelength).toPx()).toInt())
    return FloatArray(segments + 1) { length * it / segments }
}

/** How far each of [positions] departs from [center], as the two octaves displace it. */
private fun Density.sketchLineOffsets(
    positions: FloatArray,
    center: Float,
    roughness: Dp,
    tremor: Dp,
    sweepWavelength: Dp,
    tremorWavelength: Dp,
    seed: Int,
): FloatArray {
    val sweepWavelengthPx = sweepWavelength.toPx()
    val tremorWavelengthPx = tremorWavelength.toPx()
    val tremorAmplitude = tremor.toPx()

    val offsets = FloatArray(positions.size) { coherentNoise(seed, positions[it], sweepWavelengthPx) }
    normalizeToPeak(offsets, roughness.toPx())
    // The tremor octave is added raw rather than normalized: normalizing it would
    // scale every fine wave down to the single largest one, flattening the effect.
    for (index in offsets.indices) {
        val tremorNoise = coherentNoise(seed + TREMOR_SEED_OFFSET, positions[index], tremorWavelengthPx)
        offsets[index] += center + tremorNoise * tremorAmplitude
    }
    return offsets
}

/**
 * The open curve through every `(x, y)` of [xs] and [ys].
 *
 * The two anchors at either end have no neighbour to take a tangent from, so the index is
 * clamped there rather than wrapped: the curve leaves and arrives straight along itself.
 */
private fun openCurveThrough(xs: FloatArray, ys: FloatArray, tangentClamp: Float): Path {
    val segments = xs.size - 1
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
                tangentClamp = tangentClamp,
                out = controls,
            )
            cubicTo(controls[0], controls[1], controls[2], controls[3], xs[index + 1], ys[index + 1])
        }
    }
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
    tangentClamp: Float,
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

    val reach = hypot(p2x - p1x, p2y - p1y) * tangentClamp
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
        val isArc = index % 2 == 1
        if (length <= 0f) {
            // A zero-length arc is a square corner, and the duplicated anchor it leaves
            // behind is what breaks the tangent there. A zero-length straight edge means
            // something else: the radius has grown to meet the side, and the outline runs
            // smoothly across that point. A cusp there facets a pill or a circle.
            if (isArc) distances += start
        } else {
            val steps = max(if (isArc) 2 else 1, (length / anchorSpacing).roundToInt())
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

private fun cellsFor(perimeter: Float, wavelength: Float, minimumCells: Int): Int =
    max(minimumCells, (perimeter / wavelength).roundToInt())

/**
 * A deterministic value in `[-1, 1)` for a lattice point, specified so that a second
 * implementation reproduces it exactly.
 *
 * Every step is 32-bit integer arithmetic that wraps on overflow, which JavaScript
 * reproduces only through `Math.imul`; plain `*` there computes in double and loses the
 * low bits. Shifts are unsigned, and the result is taken from the top 24 bits, a width
 * both `Float` and `double` hold exactly, divided by a power of two so no rounding
 * enters either.
 */
private fun hashNoise(seed: Int, index: Int): Float {
    var h = seed * SEED_MULTIPLIER xor index * INDEX_MULTIPLIER
    h = (h xor (h ushr 13)) * MIX_MULTIPLIER
    h = h xor (h ushr 16)
    return (h ushr 8) / HASH_SCALE * 2f - 1f
}

private fun smoothstep(t: Float): Float = t * t * (3f - 2f * t)

/** Interpolates [hashNoise] over a lattice of [wavelength], for an open curve. */
private fun coherentNoise(seed: Int, position: Float, wavelength: Float): Float {
    val turns = position / wavelength
    val cell = floor(turns).toInt()
    val blend = smoothstep(turns - cell)
    return hashNoise(seed, cell) * (1f - blend) + hashNoise(seed, cell + 1) * blend
}

/**
 * The same over a lattice of [cells] wrapped onto a closed path, where [t] runs `0..1`
 * around it. Wrapping is what makes the value at the seam agree from both sides.
 */
private fun coherentNoiseCyclic(seed: Int, t: Float, cells: Int): Float {
    val turns = t * cells
    val cell = floor(turns).toInt()
    val blend = smoothstep(turns - cell)
    return hashNoise(seed, cell.mod(cells)) * (1f - blend) +
        hashNoise(seed, (cell + 1).mod(cells)) * blend
}
