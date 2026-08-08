package io.github.droidkaigi.confsched.feature.about

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context
import io.github.droidkaigi.confsched.core.model.TimetableItemId

// Architecture-demo fixture: any id present in the fake timetable works.
private val FeaturedSessionId = TimetableItemId("s6")

@Composable
context(screenContext: AboutScreenContext)
fun AboutScreenRoot(
    onNavigateToSessionDetail: (TimetableItemId) -> Unit,
    onNavigateToSponsors: () -> Unit,
    onNavigateToContributors: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    isDebugMenuAvailable: Boolean,
    onNavigateToDebug: () -> Unit,
) {
    val uiState = context(screenContext.presenterContext) {
        aboutScreenPresenter()
    }
    AboutScreen(
        uiState = uiState,
        onOpenFeaturedSession = { onNavigateToSessionDetail(FeaturedSessionId) },
        onOpenSponsors = onNavigateToSponsors,
        onOpenContributors = onNavigateToContributors,
        onOpenLicenses = onNavigateToLicenses,
        isDebugMenuAvailable = isDebugMenuAvailable,
        onOpenDebug = onNavigateToDebug,
    )
}
