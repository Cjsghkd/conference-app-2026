package io.github.droidkaigi.confsched.feature.sponsors

import io.github.droidkaigi.confsched.core.model.SponsorGroup
import kotlinx.collections.immutable.PersistentList

data class SponsorsScreenUiState(
    val groups: PersistentList<SponsorGroup>,
)
