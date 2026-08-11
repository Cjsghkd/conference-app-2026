package io.github.droidkaigi.confsched.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme

/**
 * The bar at the top of a screen: a title in the display face on one line, with a navigation
 * icon leading it and actions trailing.
 *
 * It carries its own background rather than leaving it to a `Scaffold`, so a screen can run
 * the same colour on behind whatever it puts underneath — a tab row, most often.
 *
 * @param title the text naming the screen. A screen whose content opens with a headline of its
 *   own passes an empty string, leaving the bar to the navigation icon and the actions.
 * @param modifier the [Modifier] applied to the bar.
 * @param navigationIcon the control leading the title, most often a back arrow.
 * @param containerColor the colour filling the band.
 * @param contentColor the colour the title, [navigationIcon] and [actions] draw in.
 * @param windowInsets the insets the bar holds its content clear of. The band fills behind them,
 *   so a screen drawing edge to edge keeps its colour under the status bar.
 * @param actions the controls trailing the title.
 */
@Composable
fun KaigiTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    windowInsets: WindowInsets = KaigiTopAppBarDefaults.windowInsets,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(containerColor)
                .windowInsetsPadding(windowInsets)
                .height(KaigiTopAppBarDefaults.height)
                .padding(
                    horizontal = KaigiTopAppBarDefaults.horizontalPadding,
                    vertical = KaigiTopAppBarDefaults.topPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(KaigiTopAppBarDefaults.actionSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                navigationIcon()
                BarTitle(title = title, color = contentColor)
            }
            Actions(actions)
        }
    }
}

/**
 * The bar at the top of a screen reached from another: the navigation icon and the actions on
 * one row, with the title on its own row below them.
 *
 * @param title the text naming the screen.
 * @param navigationIcon the control the row above the title leads with, most often a back arrow.
 * @param modifier the [Modifier] applied to the bar.
 * @param containerColor the colour filling the band.
 * @param contentColor the colour the title, [navigationIcon] and [actions] draw in.
 * @param windowInsets the insets the bar holds its content clear of. The band fills behind them,
 *   so a screen drawing edge to edge keeps its colour under the status bar.
 * @param actions the controls trailing [navigationIcon].
 */
@Composable
fun KaigiLargeTopAppBar(
    title: String,
    navigationIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    windowInsets: WindowInsets = KaigiTopAppBarDefaults.windowInsets,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(containerColor)
                .windowInsetsPadding(windowInsets)
                .height(KaigiTopAppBarDefaults.largeHeight)
                .padding(
                    start = KaigiTopAppBarDefaults.horizontalPadding,
                    end = KaigiTopAppBarDefaults.horizontalPadding,
                    top = KaigiTopAppBarDefaults.topPadding,
                    bottom = KaigiTopAppBarDefaults.largeBottomPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(KaigiTopAppBarDefaults.largeRowSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                navigationIcon()
                Actions(actions)
            }
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BarTitle(title = title, color = contentColor)
            }
        }
    }
}

@Composable
private fun BarTitle(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun Actions(actions: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(KaigiTopAppBarDefaults.actionSpacing),
        verticalAlignment = Alignment.CenterVertically,
        content = actions,
    )
}

object KaigiTopAppBarDefaults {
    val height = 64.dp
    val largeHeight = 112.dp
    val horizontalPadding = 24.dp
    val topPadding = 8.dp
    val largeBottomPadding = 12.dp
    val largeRowSpacing = 4.dp
    val actionSpacing = 8.dp

    val windowInsets: WindowInsets
        @Composable get() = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
}

/**
 * The back arrow every screen reached from another leads its bar with, sized and described the
 * same way in each of them.
 *
 * @param onClick called when the arrow is clicked.
 * @param modifier the [Modifier] applied to the button.
 */
@Composable
fun KaigiTopAppBarBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    KaigiIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(KaigiIconButtonDefaults.iconSize),
        )
    }
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

@Preview
@Composable
private fun KaigiLargeTopAppBarPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        KaigiLargeTopAppBar(
            title = "Contributors",
            navigationIcon = { KaigiTopAppBarBackButton(onClick = {}) },
            windowInsets = WindowInsets(0),
        )
    }
}
