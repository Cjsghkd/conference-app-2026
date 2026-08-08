package io.github.droidkaigi.confsched.core.data

import kotlinx.coroutines.delay

class FakeSponsorApi : SponsorApi {
    override suspend fun getSponsors(): SponsorListResponse {
        delay(300)
        return SponsorListResponse(
            status = HttpStatusResponse.OK,
            sponsor = listOf(
                fakeSponsor("Sponsor A", SponsorPlanResponse.PLATINUM),
                fakeSponsor("Sponsor B", SponsorPlanResponse.PLATINUM),
                fakeSponsor("Sponsor C", SponsorPlanResponse.GOLD),
                fakeSponsor("Sponsor D", SponsorPlanResponse.GOLD),
                fakeSponsor("Sponsor E", SponsorPlanResponse.GOLD),
                fakeSponsor("Sponsor F", SponsorPlanResponse.SUPPORTER),
                fakeSponsor("Sponsor G", SponsorPlanResponse.SUPPORTER),
                fakeSponsor("Sponsor H", SponsorPlanResponse.SUPPORTER),
            ),
        )
    }

    private fun fakeSponsor(
        name: String,
        plan: SponsorPlanResponse,
    ) = SponsorResponse(
        sponsorName = name,
        sponsorLogo = "https://placehold.jp/240x120.png",
        plan = plan,
        link = "https://example.com/${name.lowercase().replace(" ", "-")}",
        checkedBySponsor = true,
    )
}
