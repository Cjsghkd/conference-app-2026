package io.github.droidkaigi.confsched.feature.sponsors

import androidx.compose.ui.test.ExperimentalTestApi
import io.github.droidkaigi.confsched.core.model.Sponsor
import io.github.droidkaigi.confsched.core.model.SponsorGroup
import io.github.droidkaigi.confsched.core.model.SponsorPlan
import io.github.droidkaigi.confsched.core.model.Sponsors
import io.github.droidkaigi.confsched.core.testing.runRobotTest
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SponsorsScreenRobotTest {

    private val sampleSponsors = Sponsors(
        groups = persistentListOf(
            SponsorGroup(
                plan = SponsorPlan.Platinum,
                sponsors = persistentListOf(sponsor("Arctic Fox Inc.", SponsorPlan.Platinum)),
            ),
            SponsorGroup(
                plan = SponsorPlan.Supporter,
                sponsors = persistentListOf(sponsor("Giraffe Labs", SponsorPlan.Supporter)),
            ),
        ),
    )

    @Test
    fun sponsors_screen_behaviour() = runRobotTest(
        robotFactory = { SponsorsScreenRobot(this) },
    ) {
        describe("when the sponsors have loaded") {
            doIt {
                setupContent(sampleSponsors)
            }
            itShould("show a section per plan present in the payload") {
                checkPlanSectionDisplayed("Platinum Sponsors")
                checkPlanSectionDisplayed("Supporters")
                checkPlanSectionDoesNotExist("Gold Sponsors")
            }
            itShould("show each sponsor under its plan") {
                checkSponsorDisplayed("Arctic Fox Inc.")
                checkSponsorDisplayed("Giraffe Labs")
            }
            describe("and a sponsor is tapped") {
                doIt {
                    clickSponsor("Arctic Fox Inc.")
                }
                itShould("open that sponsor's site") {
                    checkOpenedSites("https://example.com/Arctic Fox Inc.")
                }
            }
        }
    }

    private fun sponsor(name: String, plan: SponsorPlan) = Sponsor(
        name = name,
        logoUrl = "https://example.com/$name.png",
        plan = plan,
        link = "https://example.com/$name",
    )
}
