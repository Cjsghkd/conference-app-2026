package io.github.droidkaigi.confsched.feature.sponsors

import io.github.droidkaigi.confsched.core.model.Sponsor
import io.github.droidkaigi.confsched.core.model.SponsorGroup
import io.github.droidkaigi.confsched.core.model.SponsorPlan
import io.github.droidkaigi.confsched.core.model.Sponsors
import io.github.droidkaigi.confsched.core.testing.runPresenterTest
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class SponsorsScreenPresenterTest {

    private val sampleSponsors = Sponsors(
        groups = persistentListOf(
            SponsorGroup(
                plan = SponsorPlan.Platinum,
                sponsors = persistentListOf(
                    Sponsor(
                        name = "Arctic Fox Inc.",
                        logoUrl = "https://example.com/arctic-fox.png",
                        plan = SponsorPlan.Platinum,
                        link = "https://example.com/arctic-fox",
                    ),
                ),
            ),
        ),
    )

    @Test
    fun the_loaded_groups_reach_the_ui_state_in_order() {
        runPresenterTest<SponsorsPresenterContext, Unit, Unit, SponsorsScreenUiState>(
            presenterContext = SponsorsPresenterContext(),
            presenter = { _ -> sponsorsScreenPresenter(sampleSponsors) },
        ) {
            assertEquals(sampleSponsors.groups, uiStates.awaitItem().groups)
        }
    }
}
