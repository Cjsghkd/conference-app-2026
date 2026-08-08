package io.github.droidkaigi.confsched.feature.contributors

import io.github.droidkaigi.confsched.core.model.Contributor
import io.github.droidkaigi.confsched.core.model.Contributors
import io.github.droidkaigi.confsched.core.preview.fake
import kotlinx.collections.immutable.PersistentList

data class ContributorsScreenUiState(
    val contributors: PersistentList<Contributor>,
) {
    companion object
}

internal fun ContributorsScreenUiState.Companion.fake(): ContributorsScreenUiState = ContributorsScreenUiState(
    contributors = Contributors.fake().items,
)
