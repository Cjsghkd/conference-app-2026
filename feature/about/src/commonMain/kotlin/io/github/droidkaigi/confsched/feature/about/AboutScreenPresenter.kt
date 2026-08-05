package io.github.droidkaigi.confsched.feature.about

import androidx.compose.runtime.Composable

@Composable
context(presenterContext: AboutPresenterContext)
fun aboutScreenPresenter(): AboutScreenUiState {
    return AboutScreenUiState(
        title = "About DroidKaigi 2026",
        versionName = presenterContext.buildConfig.versionName,
    )
}
