package io.github.droidkaigi.confsched.feature.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.feature.favorites.component.FavoriteDayFilterRow
import io.github.droidkaigi.confsched.feature.favorites.component.FavoritesEmptyView
import io.github.droidkaigi.confsched.feature.favorites.component.FavoritesListSection
import io.github.droidkaigi.confsched.feature.favorites.component.FavoritesListSectionUiState
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    uiState: FavoritesScreenUiState,
    onBookmarkClick: (TimetableItemId) -> Unit,
    onDayFilterClick: (DroidKaigi2026Day?) -> Unit,
    onItemClick: (TimetableItemId) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", fontWeight = FontWeight.Bold) },
                // The root tab shell already insets its content; a second system-bar inset here would double it.
                windowInsets = WindowInsets(),
            )
        },
        contentWindowInsets = WindowInsets(),
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            FavoriteDayFilterRow(selectedDayFilter = uiState.selectedDayFilter, onDayFilterClick = onDayFilterClick)

            if (uiState.favoritesListSection.timeSlots.isEmpty()) {
                FavoritesEmptyView(modifier = Modifier.weight(1f))
            } else {
                FavoritesListSection(
                    modifier = Modifier.weight(1f),
                    uiState = uiState.favoritesListSection,
                    onBookmarkClick = onBookmarkClick,
                    onItemClick = onItemClick,
                )
            }
        }
    }
}

@Preview
@Composable
fun FavoritesScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        FavoritesScreen(
            uiState = FavoritesScreenUiState.fake(),
            onBookmarkClick = {},
            onDayFilterClick = {},
            onItemClick = {},
        )
    }
}

@Preview
@Composable
fun FavoritesScreenEmptyPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        FavoritesScreen(
            uiState = FavoritesScreenUiState.fake().copy(favoritesListSection = FavoritesListSectionUiState(timeSlots = persistentListOf())),
            onBookmarkClick = {},
            onDayFilterClick = {},
            onItemClick = {},
        )
    }
}
