package io.github.droidkaigi.confsched.core.model

import kotlinx.collections.immutable.PersistentList
import kotlinx.serialization.Serializable

// Declaration order is the display order of the sponsor wall.
@Serializable
enum class SponsorPlan { Platinum, Gold, Supporter }

@Serializable
data class Sponsor(
    val name: String,
    val logoUrl: String,
    val plan: SponsorPlan,
    val link: String,
)

@Serializable
data class SponsorGroup(
    val plan: SponsorPlan,
    val sponsors: PersistentList<Sponsor>,
)

@Serializable
data class Sponsors(
    val groups: PersistentList<SponsorGroup>,
)
