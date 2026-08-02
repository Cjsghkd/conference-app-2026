package io.github.droidkaigi.confsched.feature.contributors

import io.github.droidkaigi.confsched.core.model.Contributor
import kotlinx.collections.immutable.PersistentList

data class ContributorsScreenUiState(
    val contributors: PersistentList<Contributor>,
)
