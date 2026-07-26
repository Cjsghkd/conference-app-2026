package io.github.droidkaigi.confsched.feature.favorites

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context

@Composable
context(screenContext: FavoritesScreenContext)
fun FavoritesScreenRoot() {
    val uiState = context(screenContext.presenterContext) {
        favoritesScreenPresenter()
    }
    FavoritesScreen(uiState = uiState)
}
