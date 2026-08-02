package io.github.droidkaigi.confsched.feature.contributors

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context
import io.github.droidkaigi.confsched.core.ui.SoilDataBoundary
import soil.query.compose.rememberQuery

@Composable
context(screenContext: ContributorsScreenContext)
fun ContributorsScreenRoot(
    onNavigateBack: () -> Unit,
    onNavigateToContributorProfile: (String) -> Unit,
) {
    SoilDataBoundary(state = rememberQuery(screenContext.contributorsQueryKey)) { contributors ->
        val uiState = context(screenContext.presenterContext) {
            contributorsScreenPresenter(contributors)
        }
        ContributorsScreen(
            uiState = uiState,
            onContributorClick = onNavigateToContributorProfile,
            onBackClick = onNavigateBack,
        )
    }
}
