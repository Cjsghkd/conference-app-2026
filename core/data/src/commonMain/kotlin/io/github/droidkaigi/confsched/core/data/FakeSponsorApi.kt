package io.github.droidkaigi.confsched.core.data

import kotlinx.coroutines.delay

class FakeSponsorApi : SponsorApi {
    override suspend fun getSponsors(): SponsorListResponse {
        delay(300)
        return SponsorListResponse(
            status = HttpStatusResponse.OK,
            sponsor = listOf(
                fakeSponsor("Arctic Fox Inc.", SponsorPlanResponse.PLATINUM),
                fakeSponsor("Bumblebee Corp.", SponsorPlanResponse.PLATINUM),
                fakeSponsor("Chipmunk Ltd.", SponsorPlanResponse.GOLD),
                fakeSponsor("Dolphin Studio", SponsorPlanResponse.GOLD),
                fakeSponsor("Electric Eel", SponsorPlanResponse.GOLD),
                fakeSponsor("Flamingo Works", SponsorPlanResponse.SUPPORTER),
                fakeSponsor("Giraffe Labs", SponsorPlanResponse.SUPPORTER),
                fakeSponsor("Hedgehog Design", SponsorPlanResponse.SUPPORTER),
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
        link = "https://droidkaigi.jp/2026/",
        checkedBySponsor = true,
    )
}
