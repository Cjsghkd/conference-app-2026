package io.github.droidkaigi.confsched.feature.sponsors

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.model.Sponsors

@Composable
context(_: SponsorsPresenterContext)
fun sponsorsScreenPresenter(sponsors: Sponsors): SponsorsScreenUiState {
    return SponsorsScreenUiState(groups = sponsors.groups)
}
