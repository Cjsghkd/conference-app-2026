package io.github.droidkaigi.confsched.core.data

import io.github.droidkaigi.confsched.core.model.Sponsor
import io.github.droidkaigi.confsched.core.model.SponsorGroup
import io.github.droidkaigi.confsched.core.model.SponsorPlan
import io.github.droidkaigi.confsched.core.model.Sponsors
import kotlinx.collections.immutable.toPersistentList

fun SponsorListResponse.toSponsors(): Sponsors {
    val sponsorsByPlan = sponsor.map(SponsorResponse::toSponsor).groupBy(Sponsor::plan)
    return Sponsors(
        groups = SponsorPlan.entries
            .mapNotNull { plan ->
                sponsorsByPlan[plan]?.let { SponsorGroup(plan = plan, sponsors = it.toPersistentList()) }
            }
            .toPersistentList(),
    )
}

private fun SponsorResponse.toSponsor(): Sponsor = Sponsor(
    name = sponsorName,
    logoUrl = sponsorLogo,
    plan = plan.toSponsorPlan(),
    link = link,
)

private fun SponsorPlanResponse.toSponsorPlan(): SponsorPlan = when (this) {
    SponsorPlanResponse.PLATINUM -> SponsorPlan.Platinum
    SponsorPlanResponse.GOLD -> SponsorPlan.Gold
    SponsorPlanResponse.SUPPORTER -> SponsorPlan.Supporter
}
