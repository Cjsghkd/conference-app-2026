package io.github.droidkaigi.confsched.core.testing

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest

@OptIn(ExperimentalTestApi::class)
abstract class Robot(protected val composeUiTest: ComposeUiTest)

@OptIn(ExperimentalTestApi::class)
fun <R : Robot> runRobotTest(
    robotFactory: ComposeUiTest.() -> R,
    scenario: ScenarioBuilder<R>.() -> Unit,
) {
    val leaves = ScenarioBuilder<R>().apply(scenario).build().flatten()
    check(leaves.isNotEmpty()) { "Robot scenario has no itShould blocks" }
    for (leaf in leaves) {
        try {
            runComposeUiTest {
                val robot = robotFactory()
                leaf.setups.forEach { step -> step(robot) }
                leaf.check(robot)
            }
        } catch (error: Throwable) {
            throw AssertionError("Scenario failed: ${leaf.name}", error)
        }
    }
}
