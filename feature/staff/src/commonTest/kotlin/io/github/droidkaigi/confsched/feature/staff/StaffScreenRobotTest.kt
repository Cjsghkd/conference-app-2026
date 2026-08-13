package io.github.droidkaigi.confsched.feature.staff

import androidx.compose.ui.test.ExperimentalTestApi
import io.github.droidkaigi.confsched.core.model.Staff
import io.github.droidkaigi.confsched.core.model.StaffId
import io.github.droidkaigi.confsched.core.model.StaffMember
import io.github.droidkaigi.confsched.core.testing.RobotTest
import io.github.droidkaigi.confsched.core.testing.runRobotTest
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class StaffScreenRobotTest : RobotTest() {

    private val sampleStaff = Staff(
        items = persistentListOf(staffMember(1L, "staff-a"), staffMember(2L, "staff-b")),
    )

    @Test
    fun staff_screen_behaviour() = runRobotTest(
        robotFactory = { StaffScreenRobot(this) },
    ) {
        describe("when the staff have loaded") {
            doIt {
                setupStaff(sampleStaff)
                setupContent()
            }
            itShould("list every staff member") {
                checkStaffDisplayed("staff-a")
                checkStaffDisplayed("staff-b")
            }
            describe("and a staff member is tapped") {
                doIt {
                    clickStaff("staff-b")
                }
                itShould("open that person's profile") {
                    checkOpenedProfiles("https://example.com/staff-b")
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
    }

    private fun staffMember(id: Long, username: String) = StaffMember(
        id = StaffId(id),
        username = username,
        iconUrl = "https://example.com/$username.png",
        profileUrl = "https://example.com/$username",
    )
}
