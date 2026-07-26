package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
context(presenterContext: SoilErrorsPresenterContext)
fun soilErrorsScreenPresenter(): SoilErrorsScreenUiState {
    val errors by presenterContext.soilErrorMonitor.errors.collectAsState()

    return SoilErrorsScreenUiState(
        errors = errors,
    )
}
