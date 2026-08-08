package io.github.droidkaigi.confsched.feature.sessions.timetable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.safeClick
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.TimetableItemDetailHeadline
import io.github.droidkaigi.confsched.feature.sessions.timetable.component.TimetableItemDetailSummaryCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TimetableItemDetailScreen(
    uiState: TimetableItemDetailScreenUiState,
    onBookmarkClick: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                // The headline below carries the same background, so the two read as one surface.
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                navigationIcon = {
                    IconButton(onClick = safeClick(onBack)) {
                        if (LocalListDetailSceneScope.current != null) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = safeClick(onBookmarkClick)) {
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
                    room = uiState.item.room,
                    title = uiState.item.title,
                    speaker = uiState.item.speaker,
                )
            }
            item {
                TimetableItemDetailSummaryCard(
                    day = uiState.item.day,
                    startsAt = uiState.item.startsAt,
                    endsAt = uiState.item.endsAt,
                    room = uiState.item.room,
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
            uiState = TimetableItemDetailScreenUiState(
                item = TimetableItem(
                    id = TimetableItemId("d1a"),
                    title = "Compose Multiplatform in Practice",
                    room = "Arctic Fox",
                    speaker = "Speaker A",
                    day = DroidKaigi2026Day.Day1,
                    startsAt = "10:00",
                    endsAt = "10:40",
                ),
                isFavorite = true,
            ),
            onBookmarkClick = {},
            onBack = {},
        )
    }
}
