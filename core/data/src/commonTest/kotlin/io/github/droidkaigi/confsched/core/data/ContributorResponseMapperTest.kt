package io.github.droidkaigi.confsched.core.data

import io.github.droidkaigi.confsched.core.model.ContributorId
import kotlin.test.Test
import kotlin.test.assertEquals

class ContributorResponseMapperTest {

    @Test
    fun contributors_keep_their_payload_order() {
        val contributors = ContributorListResponse(
            status = HttpStatusResponse.OK,
            contributors = listOf(contributorResponse(2L, "user-b"), contributorResponse(1L, "user-a")),
        ).toContributors()

        assertEquals(listOf("user-b", "user-a"), contributors.items.map { it.username })
        assertEquals(listOf(ContributorId(2L), ContributorId(1L)), contributors.items.map { it.id })
    }

    @Test
    fun the_largest_icon_variant_becomes_the_list_avatar() {
        val contributors = ContributorListResponse(
            status = HttpStatusResponse.OK,
            contributors = listOf(contributorResponse(1L, "user-a")),
        ).toContributors()

        assertEquals("https://example.com/user-a/128.png", contributors.items.single().iconUrl)
    }

    private fun contributorResponse(id: Long, username: String) = ContributorResponse(
        id = id,
        username = username,
        iconUrl = "https://example.com/$username/original.png",
        icon32Url = "https://example.com/$username/32.png",
        icon64Url = "https://example.com/$username/64.png",
        icon128Url = "https://example.com/$username/128.png",
        profileUrl = "https://example.com/$username",
    )
}
