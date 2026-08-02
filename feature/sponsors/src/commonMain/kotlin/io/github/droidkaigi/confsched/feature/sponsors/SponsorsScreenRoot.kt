package io.github.droidkaigi.confsched.feature.sponsors

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context
import io.github.droidkaigi.confsched.core.ui.SoilDataBoundary
import soil.query.compose.rememberQuery

@Composable
context(screenContext: SponsorsScreenContext)
fun SponsorsScreenRoot(
    onNavigateBack: () -> Unit,
    onNavigateToSponsorSite: (String) -> Unit,
) {
    SoilDataBoundary(state = rememberQuery(screenContext.sponsorsQueryKey)) { sponsors ->
        val uiState = context(screenContext.presenterContext) {
            sponsorsScreenPresenter(sponsors)
        }
        SponsorsScreen(
            uiState = uiState,
            onSponsorClick = onNavigateToSponsorSite,
            onBackClick = onNavigateBack,
        )
    }
}
