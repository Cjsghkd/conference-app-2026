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
            Contributor(
                id = ContributorId(1L),
                username = "alice",
                iconUrl = "https://example.com/alice.png",
                profileUrl = "https://github.com/alice",
            ),
        ),
    )

    @Test
    fun the_loaded_contributors_reach_the_ui_state_in_order() {
        runPresenterTest<ContributorsPresenterContext, Unit, Unit, ContributorsScreenUiState>(
            presenterContext = ContributorsPresenterContext(),
            presenter = { _ -> contributorsScreenPresenter(sampleContributors) },
        ) {
            assertEquals(sampleContributors.items, uiStates.awaitItem().contributors)
        }
    }
}
