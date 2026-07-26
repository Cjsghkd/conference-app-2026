package io.github.droidkaigi.confsched.feature.about

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.model.TimetableItemId

@Composable
context(presenterContext: AboutPresenterContext)
fun aboutScreenPresenter(): AboutScreenUiState {
    return AboutScreenUiState(
        title = "About DroidKaigi 2026",
        versionName = presenterContext.buildConfig.versionName,
        // Architecture-demo fixture: any id present in the fake timetable works.
        featuredSessionId = TimetableItemId("s6"),
    )
}
