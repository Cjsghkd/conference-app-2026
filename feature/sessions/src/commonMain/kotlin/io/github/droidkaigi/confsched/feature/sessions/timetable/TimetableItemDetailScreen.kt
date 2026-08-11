package io.github.droidkaigi.confsched.feature.sessions.timetable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.KaigiIconButton
import io.github.droidkaigi.confsched.core.ui.KaigiIconButtonDefaults
import io.github.droidkaigi.confsched.core.ui.KaigiTopAppBar
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.TimetableItemDetailHeadline
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.TimetableItemDetailSummaryCard

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TimetableItemDetailScreen(
    uiState: TimetableItemDetailScreenUiState,
    onBookmarkClick: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            KaigiTopAppBar(
                title = "",
                navigationIcon = {
                    val dismissing = LocalListDetailSceneScope.current != null
                    KaigiIconButton(onClick = onBack) {
                        Icon(
                            imageVector = if (dismissing) {
                                Icons.Filled.Close
                            } else {
                                Icons.AutoMirrored.Filled.ArrowBack
                            },
                            contentDescription = if (dismissing) "Close" else "Back",
                            modifier = Modifier.size(KaigiIconButtonDefaults.iconSize),
                        )
                    }
                },
                // The headline below carries the same background, so the two read as one surface.
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onBookmarkClick) {
                Icon(
                    imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (uiState.isFavorite) "Remove favorite" else "Add favorite",
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = TimetableItemDetailScreenDefaults.floatingActionButtonClearance),
        ) {
            item {
                TimetableItemDetailHeadline(
                    room = uiState.item.room.name,
                    title = uiState.item.title,
                    speaker = uiState.item.speaker,
                )
            }
            item {
                TimetableItemDetailSummaryCard(
                    day = uiState.item.day,
                    startsAt = uiState.item.startsAt,
                    endsAt = uiState.item.endsAt,
                    room = uiState.item.room.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                )
            }
        }
    }
}

private object TimetableItemDetailScreenDefaults {
    val floatingActionButtonClearance = 88.dp
}

@Preview
@Composable
fun TimetableItemDetailScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        TimetableItemDetailScreen(
            uiState = TimetableItemDetailScreenUiState.fake(),
            onBookmarkClick = {},
            onBack = {},
        )
    }
}
