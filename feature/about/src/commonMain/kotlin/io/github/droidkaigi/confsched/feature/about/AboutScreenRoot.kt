package io.github.droidkaigi.confsched.feature.about

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context
import io.github.droidkaigi.confsched.core.model.TimetableItemId

@Composable
context(screenContext: AboutScreenContext)
fun AboutScreenRoot(
    onNavigateToSessionDetail: (TimetableItemId) -> Unit,
    onNavigateToSponsors: () -> Unit,
    onNavigateToContributors: () -> Unit,
    isDebugMenuAvailable: Boolean,
    onNavigateToDebug: () -> Unit,
) {
    val uiState = context(screenContext.presenterContext) {
        aboutScreenPresenter()
    }
    AboutScreen(
        uiState = uiState,
        onOpenFeaturedSession = onNavigateToSessionDetail,
        onOpenSponsors = onNavigateToSponsors,
        onOpenContributors = onNavigateToContributors,
        isDebugMenuAvailable = isDebugMenuAvailable,
        onOpenDebug = onNavigateToDebug,
    )
}
