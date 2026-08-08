package io.github.droidkaigi.confsched.feature.about

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context
import io.github.droidkaigi.confsched.core.ui.SoilDataBoundary
import soil.query.compose.rememberQuery

@Composable
context(screenContext: LicensesScreenContext)
fun LicensesScreenRoot(onNavigateBack: () -> Unit) {
    SoilDataBoundary(state = rememberQuery(screenContext.licensesQueryKey)) { libs ->
        val uiState = context(screenContext.presenterContext) {
            licensesScreenPresenter(libs)
        }
        LicensesScreen(
            uiState = uiState,
            onBackClick = onNavigateBack,
        )
    }
}
