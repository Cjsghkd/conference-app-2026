package io.github.droidkaigi.confsched.feature.sponsors

import io.github.droidkaigi.confsched.core.model.SponsorGroup
import io.github.droidkaigi.confsched.core.model.Sponsors
import io.github.droidkaigi.confsched.core.preview.fake
import kotlinx.collections.immutable.PersistentList

data class SponsorsScreenUiState(
    val groups: PersistentList<SponsorGroup>,
) {
    companion object
}

internal fun SponsorsScreenUiState.Companion.fake(): SponsorsScreenUiState = SponsorsScreenUiState(
    groups = Sponsors.fake().groups,
)
