package io.github.droidkaigi.confsched.feature.contributors

import dev.zacsweers.metro.createGraph
import io.github.droidkaigi.confsched.core.model.Contributor
import io.github.droidkaigi.confsched.core.model.ContributorId
import io.github.droidkaigi.confsched.core.model.Contributors
import io.github.droidkaigi.confsched.core.testing.runPresenterTest
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class ContributorsScreenPresenterTest {

    private val graph = createGraph<ContributorsScreenTestGraph>()

    private val sampleContributors = Contributors(
        items = persistentListOf(
            contributor(2L, "user-b"),
            contributor(1L, "user-a"),
        ),
    )

    @Test
    fun the_loaded_contributors_reach_the_ui_state_in_order() {
        runPresenterTest<ContributorsPresenterContext, Unit, Unit, ContributorsScreenUiState>(
            presenterContext = graph.presenterContext,
            presenter = { _ -> contributorsScreenPresenter(sampleContributors) },
        ) {
            val contributors = uiStates.awaitItem().contributors
            assertEquals(sampleContributors.items, contributors)
            assertEquals(listOf("user-b", "user-a"), contributors.map { it.username })
        }
    }

    private fun contributor(id: Long, username: String) = Contributor(
        id = ContributorId(id),
        username = username,
        iconUrl = "https://example.com/$username.png",
        profileUrl = "https://example.com/$username",
    )
}
