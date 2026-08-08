package io.github.droidkaigi.confsched.feature.favorites

import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.feature.favorites.component.FavoritesListSectionUiState
import io.github.droidkaigi.confsched.feature.favorites.component.fake

data class FavoritesScreenUiState(
    val selectedDayFilter: DroidKaigi2026Day?,
    val favoritesListSection: FavoritesListSectionUiState,
) {
    companion object
}

internal fun FavoritesScreenUiState.Companion.fake(): FavoritesScreenUiState = FavoritesScreenUiState(
    selectedDayFilter = null,
    favoritesListSection = FavoritesListSectionUiState.fake(),
)
