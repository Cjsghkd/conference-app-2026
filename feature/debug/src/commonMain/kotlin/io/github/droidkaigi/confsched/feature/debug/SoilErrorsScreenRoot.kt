package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context

context(screenContext: SoilErrorsScreenContext)
@Composable
fun SoilErrorsScreenRoot(
    onNavigateBack: () -> Unit,
) {
    val uiState = context(screenContext.presenterContext) {
        soilErrorsScreenPresenter()
    }
    SoilErrorsScreen(
        uiState = uiState,
        onBackClick = onNavigateBack,
    )
}
