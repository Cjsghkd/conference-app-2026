package io.github.droidkaigi.confsched.feature.about

import androidx.compose.ui.test.ExperimentalTestApi
import io.github.droidkaigi.confsched.core.testing.runRobotTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LicensesScreenRobotTest {

    private val sampleLibs = LicensesScreenUiState.fake().libs

    @Test
    fun licenses_screen_behaviour() = runRobotTest(
        robotFactory = { LicensesScreenRobot(this) },
    ) {
        describe("when the collected libraries have loaded") {
            doIt {
                setupLibs(sampleLibs)
                setupContent()
            }
            itShould("list every library with its version") {
                checkTextDisplayed("Library A")
                checkTextDisplayed("1.0.0")
                checkTextDisplayed("Library B")
                checkTextDisplayed("2.3.1")
            }
            describe("and a library row is tapped") {
                doIt {
                    clickLibrary("Library A")
                }
                itShould("expand that library's actions") {
                    checkTextDisplayed("View license")
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

        describe("when the libraries have not arrived yet") {
            doIt {
                setupPendingLibs()
                setupContent()
            }
            itShould("show the loading fallback") {
                checkLoadingDisplayed()
                checkTextDoesNotExist("Library A")
            }
            describe("and they arrive") {
                doIt {
                    releaseLibs(sampleLibs)
                }
                itShould("swap the fallback for the content") {
                    checkTextDisplayed("Library A")
                }
            }
        }

        describe("when the libraries fail to load") {
            doIt {
                setupFailingLibs()
                setupContent()
            }
            itShould("show the error fallback") {
                checkErrorDisplayed()
            }
        }
    }
}
