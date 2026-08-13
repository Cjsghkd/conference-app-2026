package io.github.droidkaigi.confsched.feature.about

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context

@Composable
context(screenContext: AboutScreenContext)
fun AboutScreenRoot(
    onNavigateToSponsors: () -> Unit,
    onNavigateToContributors: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    isDebugMenuAvailable: Boolean,
    onNavigateToDebug: () -> Unit,
) {
    val uiState = context(screenContext.presenterContext) {
        aboutScreenPresenter()
    }
    AboutScreen(
        uiState = uiState,
        onOpenSponsors = onNavigateToSponsors,
        onOpenContributors = onNavigateToContributors,
        onOpenStaff = onNavigateToStaff,
        onOpenLicenses = onNavigateToLicenses,
        isDebugMenuAvailable = isDebugMenuAvailable,
        onOpenDebug = onNavigateToDebug,
    )
}
