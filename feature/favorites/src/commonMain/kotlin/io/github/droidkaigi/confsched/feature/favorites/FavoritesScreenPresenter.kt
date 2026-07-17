package io.github.droidkaigi.confsched.feature.favorites

import androidx.compose.runtime.Composable

context(_: FavoritesPresenterContext)
@Composable
fun favoritesScreenPresenter(): FavoritesScreenUiState {
    return FavoritesScreenUiState(
        title = "Favorites",
    )
}
