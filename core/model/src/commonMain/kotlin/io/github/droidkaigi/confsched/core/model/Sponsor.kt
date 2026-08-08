package io.github.droidkaigi.confsched.core.model

import kotlinx.collections.immutable.PersistentList

// Declaration order is the display order of the sponsor wall.
enum class SponsorPlan { Platinum, Gold, Supporter }

data class Sponsor(
    val name: String,
    val logoUrl: String,
    val plan: SponsorPlan,
    val link: String,
)

data class SponsorGroup(
    val plan: SponsorPlan,
    val sponsors: PersistentList<Sponsor>,
)

data class Sponsors(
    val groups: PersistentList<SponsorGroup>,
) {
    companion object
}
