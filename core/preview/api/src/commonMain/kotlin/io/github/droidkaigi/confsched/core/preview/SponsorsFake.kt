package io.github.droidkaigi.confsched.core.preview

import io.github.droidkaigi.confsched.core.model.Sponsor
import io.github.droidkaigi.confsched.core.model.SponsorGroup
import io.github.droidkaigi.confsched.core.model.SponsorPlan
import io.github.droidkaigi.confsched.core.model.Sponsors
import kotlinx.collections.immutable.persistentListOf

fun Sponsors.Companion.fake(): Sponsors = Sponsors(
    groups = persistentListOf(
        SponsorGroup(
            plan = SponsorPlan.Platinum,
            sponsors = persistentListOf(
                fakeSponsor("Sponsor A", SponsorPlan.Platinum),
                fakeSponsor("Sponsor B", SponsorPlan.Platinum),
            ),
        ),
        SponsorGroup(
            plan = SponsorPlan.Gold,
            sponsors = persistentListOf(
                fakeSponsor("Sponsor C", SponsorPlan.Gold),
                fakeSponsor("Sponsor D", SponsorPlan.Gold),
            ),
        ),
        SponsorGroup(
            plan = SponsorPlan.Supporter,
            sponsors = persistentListOf(
                fakeSponsor("Sponsor E", SponsorPlan.Supporter),
                fakeSponsor("Sponsor F", SponsorPlan.Supporter),
                fakeSponsor("Sponsor G", SponsorPlan.Supporter),
            ),
        ),
    ),
)

private fun fakeSponsor(name: String, plan: SponsorPlan) = Sponsor(
    name = name,
    logoUrl = PreviewImage.SessionCover.imageUrl,
    plan = plan,
    link = "https://example.com/${name.lowercase().replace(" ", "-")}",
)
