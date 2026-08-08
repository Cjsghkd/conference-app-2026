package io.github.droidkaigi.confsched.core.data

import kotlinx.coroutines.delay

class FakeContributorsApi : ContributorsApi {
    override suspend fun getContributors(): ContributorListResponse {
        delay(300)
        return ContributorListResponse(
            status = HttpStatusResponse.OK,
            contributors = listOf(
                fakeContributor(1L, "user-a"),
                fakeContributor(2L, "user-b"),
                fakeContributor(3L, "user-c"),
                fakeContributor(4L, "user-d"),
                fakeContributor(5L, "user-e"),
            ),
        )
    }

    private fun fakeContributor(
        id: Long,
        username: String,
    ) = ContributorResponse(
        id = id,
        username = username,
        iconUrl = "https://placehold.jp/128x128.png",
        icon32Url = "https://placehold.jp/32x32.png",
        icon64Url = "https://placehold.jp/64x64.png",
        icon128Url = "https://placehold.jp/128x128.png",
        profileUrl = "https://example.com/$username",
    )
}
