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
                fakeSponsor("Arctic Fox Inc.", SponsorPlan.Platinum),
                fakeSponsor("Bumblebee Corp.", SponsorPlan.Platinum),
            ),
        ),
        SponsorGroup(
            plan = SponsorPlan.Gold,
            sponsors = persistentListOf(
                fakeSponsor("Chipmunk Ltd.", SponsorPlan.Gold),
                fakeSponsor("Dolphin Studio", SponsorPlan.Gold),
            ),
        ),
        SponsorGroup(
            plan = SponsorPlan.Supporter,
            sponsors = persistentListOf(
                fakeSponsor("Electric Eel", SponsorPlan.Supporter),
                fakeSponsor("Flamingo Works", SponsorPlan.Supporter),
                fakeSponsor("Giraffe Labs", SponsorPlan.Supporter),
            ),
        ),
    ),
)

private fun fakeSponsor(name: String, plan: SponsorPlan) = Sponsor(
    name = name,
    logoUrl = PreviewImage.SessionCover.imageUrl,
    plan = plan,
    link = "https://droidkaigi.jp/2026/",
)
