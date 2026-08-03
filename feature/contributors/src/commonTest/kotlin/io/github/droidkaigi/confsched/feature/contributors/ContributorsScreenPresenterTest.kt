package io.github.droidkaigi.confsched.feature.contributors

import io.github.droidkaigi.confsched.core.model.Contributor
import io.github.droidkaigi.confsched.core.model.ContributorId
import io.github.droidkaigi.confsched.core.model.Contributors
import io.github.droidkaigi.confsched.core.testing.runPresenterTest
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class ContributorsScreenPresenterTest {

    private val sampleContributors = Contributors(
        items = persistentListOf(
            contributor(2L, "bob"),
            contributor(1L, "alice"),
        ),
    )

    @Test
    fun the_loaded_contributors_reach_the_ui_state_in_order() {
        runPresenterTest<ContributorsPresenterContext, Unit, Unit, ContributorsScreenUiState>(
            presenterContext = ContributorsPresenterContext(),
            presenter = { _ -> contributorsScreenPresenter(sampleContributors) },
        ) {
            val contributors = uiStates.awaitItem().contributors
            assertEquals(sampleContributors.items, contributors)
            assertEquals(listOf("bob", "alice"), contributors.map { it.username })
        }
    }

    private fun contributor(id: Long, username: String) = Contributor(
        id = ContributorId(id),
        username = username,
        iconUrl = "https://example.com/$username.png",
        profileUrl = "https://github.com/$username",
    )
}
