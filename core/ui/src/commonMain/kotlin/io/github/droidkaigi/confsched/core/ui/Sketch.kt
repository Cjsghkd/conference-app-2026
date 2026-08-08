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

/**
 * A horizontal rule drawn as a single hand-sketched stroke.
 *
 * The same [seed] always produces the same line, so the stroke stays stable
 * across recompositions; use a different [seed] per call site to avoid two
 * neighbouring dividers sharing an identical wobble.
 *
 * [roughness] sets how far the broad sweep departs from a straight line, and
 * [tremor] adds a finer wobble on top of it, as a fraction of [roughness].
 */
@Composable
fun SketchDivider(
    seed: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline,
    thickness: Dp = 2.dp,
    roughness: Dp = 3.dp,
    tremor: Float = 0.25f,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness + roughness * (1f + tremor) * 2)
            .drawWithCache {
                val path = sketchLinePath(
                    width = size.width,
                    centerY = size.height / 2f,
                    roughness = roughness,
                    tremor = tremor,
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
    roughness: Dp = 3.dp,
    tremor: Float = 0.25f,
    cornerRadius: Dp = 0.dp,
): Modifier = drawWithCache {
    val swing = roughness * swingCapRatio(size.width, size.height, roughness, tremor)
    val inset = swing.toPx() * (1f + tremor) + thickness.toPx() / 2f
    val width = size.width - inset * 2f
    val height = size.height - inset * 2f
    if (width <= 0f || height <= 0f) return@drawWithCache onDrawBehind {}

    val path = sketchRoundRectPath(
        width = width,
        height = height,
        cornerRadius = cornerRadius,
        roughness = swing,
        tremor = tremor,
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
 * The outline is inset by `roughness * (1 + tremor)` so the whole wobble stays within
 * the bounds. [Modifier.sketchBorder] insets a further half of its stroke width to keep
 * the line itself inside, so pairing the two leaves that half-width between them.
 *
 * On a box too small to hold the requested swing, both octaves shrink together rather
 * than letting the outline fold onto itself.
 */
@Immutable
data class SketchShape(
    val seed: Int,
    val roughness: Dp = 3.dp,
    val tremor: Float = 0.25f,
    val cornerRadius: Dp = 0.dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = with(density) {
        val swing = roughness * swingCapRatio(size.width, size.height, roughness, tremor)
        val inset = swing.toPx() * (1f + tremor)
        val width = size.width - inset * 2f
        val height = size.height - inset * 2f
        if (width <= 0f || height <= 0f) return Outline.Rectangle(size.toRect())

        val path = sketchRoundRectPath(
            width = width,
            height = height,
            cornerRadius = cornerRadius,
            roughness = swing,
            tremor = tremor,
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
private fun SketchDividerTremorPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            TremorSamples()
        }
    }
}

@Composable
private fun TremorSamples() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(0f, 0.25f, 0.6f, 1f).forEach { tremor ->
            LabelledSample(label = "tremor=$tremor") {
                SketchDivider(seed = 7, tremor = tremor)
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
private fun SketchBorderRoughnessPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        PreviewSurface {
            RoughnessSamples()
        }
    }
}

@Composable
private fun RoughnessSamples() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(1.dp, 3.dp, 5.dp, 8.dp).forEachIndexed { index, roughness ->
            LabelledSample(label = "±${roughness.value.toInt()}") {
                Box(
                    Modifier
                        .size(90.dp, 64.dp)
                        .sketchBorder(
                            seed = 30 + index,
                            color = MaterialTheme.colorScheme.outline,
                            roughness = roughness,
                            cornerRadius = 12.dp,
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
                            .clip(SketchShape(seed = 53, tremor = 1f, cornerRadius = 12.dp))
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
