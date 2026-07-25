package io.github.droidkaigi.confsched.feature.about

import io.github.droidkaigi.confsched.core.model.TimetableItemId

data class AboutScreenUiState(
    val title: String,
    val versionName: String,
    val featuredSessionId: TimetableItemId,
)
