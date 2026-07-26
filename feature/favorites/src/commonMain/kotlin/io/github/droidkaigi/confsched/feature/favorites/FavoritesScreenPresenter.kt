package io.github.droidkaigi.confsched.feature.favorites

import androidx.compose.runtime.Composable

@Composable
context(_: FavoritesPresenterContext)
fun favoritesScreenPresenter(): FavoritesScreenUiState {
    return FavoritesScreenUiState(
        title = "Favorites",
    )
}
