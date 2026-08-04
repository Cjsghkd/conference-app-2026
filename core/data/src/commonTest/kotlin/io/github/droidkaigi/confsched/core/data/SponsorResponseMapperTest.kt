package io.github.droidkaigi.confsched.core.data

import io.github.droidkaigi.confsched.core.model.SponsorPlan
import kotlin.test.Test
import kotlin.test.assertEquals

class SponsorResponseMapperTest {

    @Test
    fun groups_are_ordered_by_plan_regardless_of_payload_order() {
        val sponsors = sponsorListResponse(
            sponsorResponse("Supporter A", SponsorPlanResponse.SUPPORTER),
            sponsorResponse("Gold A", SponsorPlanResponse.GOLD),
            sponsorResponse("Platinum A", SponsorPlanResponse.PLATINUM),
        ).toSponsors()

        assertEquals(
            listOf(SponsorPlan.Platinum, SponsorPlan.Gold, SponsorPlan.Supporter),
            sponsors.groups.map { it.plan },
        )
    }

    @Test
    fun sponsors_keep_their_payload_order_within_a_plan() {
        val sponsors = sponsorListResponse(
            sponsorResponse("Gold A", SponsorPlanResponse.GOLD),
            sponsorResponse("Platinum A", SponsorPlanResponse.PLATINUM),
            sponsorResponse("Gold B", SponsorPlanResponse.GOLD),
        ).toSponsors()

        val gold = sponsors.groups.first { it.plan == SponsorPlan.Gold }
        assertEquals(listOf("Gold A", "Gold B"), gold.sponsors.map { it.name })
    }

    @Test
    fun a_plan_without_sponsors_produces_no_group() {
        val sponsors = sponsorListResponse(
            sponsorResponse("Platinum A", SponsorPlanResponse.PLATINUM),
        ).toSponsors()

        assertEquals(listOf(SponsorPlan.Platinum), sponsors.groups.map { it.plan })
    }

    @Test
    fun an_empty_payload_produces_no_groups() {
        assertEquals(emptyList(), sponsorListResponse().toSponsors().groups)
    }

    private fun sponsorListResponse(vararg sponsors: SponsorResponse) = SponsorListResponse(
        status = HttpStatusResponse.OK,
        sponsor = sponsors.toList(),
    )

    private fun sponsorResponse(name: String, plan: SponsorPlanResponse) = SponsorResponse(
        sponsorName = name,
        sponsorLogo = "https://example.com/$name.png",
        plan = plan,
        link = "https://example.com/$name",
        checkedBySponsor = true,
    )
}
