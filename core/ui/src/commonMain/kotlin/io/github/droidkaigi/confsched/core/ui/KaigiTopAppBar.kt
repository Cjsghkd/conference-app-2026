package io.github.droidkaigi.confsched.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme

/**
 * The bar at the top of a root screen: a title in the display face, with actions trailing.
 *
 * It carries its own background rather than leaving it to a `Scaffold`, so a screen can run
 * the same colour on behind whatever it puts underneath — a tab row, most often.
 *
 * @param title the text naming the screen.
 * @param modifier the [Modifier] applied to the bar.
 * @param containerColor the colour filling the band.
 * @param contentColor the colour the title and [actions] draw in.
 * @param windowInsets the insets the bar holds its content clear of. The band fills behind them,
 *   so a screen drawing edge to edge keeps its colour under the status bar.
 * @param actions the controls trailing the title.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaigiTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = title, style = MaterialTheme.typography.headlineMedium) },
        modifier = modifier,
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(KaigiTopAppBarDefaults.actionSpacing),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        },
        expandedHeight = KaigiTopAppBarDefaults.height,
        windowInsets = windowInsets,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            actionIconContentColor = contentColor,
        ),
        contentPadding = ContentPadding,
    )
}

/**
 * The design asks for 16.dp at the trailing edge, and [TopAppBar] already insets its action
 * slot by 4.dp, so this carries the remainder. The leading edge needs none: the title inset
 * [TopAppBar] applies with no navigation icon comes to the same 16.dp on its own.
 */
private val ContentPadding = PaddingValues(end = 12.dp)

object KaigiTopAppBarDefaults {
    val height = 64.dp
    val actionSpacing = 8.dp
}

@Preview
@Composable
private fun KaigiTopAppBarPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        KaigiTopAppBar(title = "Timetable", windowInsets = WindowInsets(0)) {
            KaigiIconButton(seed = 777, onClick = {}) {
                Icon(Icons.Filled.Search, contentDescription = null)
            }
            KaigiIconButton(seed = 778, onClick = {}) {
                Icon(Icons.Filled.DateRange, contentDescription = null)
            }
        }
    }
}
