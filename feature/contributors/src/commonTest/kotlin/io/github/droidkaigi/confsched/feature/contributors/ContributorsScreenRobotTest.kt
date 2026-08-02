package io.github.droidkaigi.confsched.feature.contributors

import androidx.compose.ui.test.ExperimentalTestApi
import io.github.droidkaigi.confsched.core.model.Contributor
import io.github.droidkaigi.confsched.core.model.ContributorId
import io.github.droidkaigi.confsched.core.model.Contributors
import io.github.droidkaigi.confsched.core.testing.runRobotTest
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ContributorsScreenRobotTest {

    private val sampleContributors = Contributors(
        items = persistentListOf(contributor(1L, "alice"), contributor(2L, "bob")),
    )

    @Test
    fun contributors_screen_behaviour() = runRobotTest(
        robotFactory = { ContributorsScreenRobot(this) },
    ) {
        describe("when the contributors have loaded") {
            doIt {
                setupContent(sampleContributors)
            }
            itShould("list every contributor") {
                checkContributorDisplayed("alice")
                checkContributorDisplayed("bob")
            }
            itShould("show how many there are") {
                checkCountDisplayed(2)
            }
            describe("and a contributor is tapped") {
                doIt {
                    clickContributor("bob")
                }
                itShould("open that contributor's profile") {
                    checkOpenedProfiles("https://github.com/bob")
                }
            }
        }
    }

    private fun contributor(id: Long, username: String) = Contributor(
        id = ContributorId(id),
        username = username,
        iconUrl = "https://example.com/$username.png",
        profileUrl = "https://github.com/$username",
    )
}
