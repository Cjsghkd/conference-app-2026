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
                sponsors = persistentListOf(sponsor("Arctic Fox Inc.", "arctic-fox", SponsorPlan.Platinum)),
            ),
            SponsorGroup(
                plan = SponsorPlan.Supporter,
                sponsors = persistentListOf(sponsor("Giraffe Labs", "giraffe-labs", SponsorPlan.Supporter)),
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
                    checkOpenedSites("https://example.com/arctic-fox")
                }
            }
            describe("and back is tapped") {
                doIt {
                    clickBack()
                }
                itShould("leave the screen once") {
                    checkBackInvoked(times = 1)
                }
            }
        }

        describe("when two sponsors on one plan share a name") {
            doIt {
                setupContent(
                    Sponsors(
                        groups = persistentListOf(
                            SponsorGroup(
                                plan = SponsorPlan.Gold,
                                sponsors = persistentListOf(
                                    sponsor("Dolphin Studio", "dolphin-jp", SponsorPlan.Gold),
                                    sponsor("Dolphin Studio", "dolphin-us", SponsorPlan.Gold),
                                ),
                            ),
                        ),
                    ),
                )
            }
            itShould("render both instead of failing on a duplicate key") {
                checkPlanSectionDisplayed("Gold Sponsors")
                checkSponsorCount("Dolphin Studio", expected = 2)
            }
        }

        describe("when the payload carries no sponsors") {
            doIt {
                setupContent(Sponsors(groups = persistentListOf()))
            }
            itShould("show the empty state") {
                checkEmptyStateDisplayed()
                checkPlanSectionDoesNotExist("Platinum Sponsors")
            }
        }
    }

    private fun sponsor(name: String, slug: String, plan: SponsorPlan) = Sponsor(
        name = name,
        logoUrl = "https://example.com/$slug.png",
        plan = plan,
        link = "https://example.com/$slug",
    )
}
