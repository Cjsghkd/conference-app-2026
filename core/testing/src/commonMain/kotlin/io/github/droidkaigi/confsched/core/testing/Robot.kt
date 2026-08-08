package io.github.droidkaigi.confsched.core.testing

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import io.github.droidkaigi.confsched.core.common.LocalSafeClickInvoker
import io.github.droidkaigi.confsched.core.common.LocalSnackbarHostState
import io.github.droidkaigi.confsched.core.common.SafeClickInvoker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import soil.query.SwrCachePlus
import soil.query.compose.SwrClientProvider
import kotlin.time.Duration

@OptIn(ExperimentalTestApi::class)
abstract class Robot(protected val composeUiTest: ComposeUiTest) {

    // Stands in for what a nav entry supplies in production: the Soil client, the snackbar host
    // from snackbarNavEntryDecorator, and the click debounce — zeroed so consecutive taps in one
    // scenario are not swallowed. A fresh client per call lets a scenario set up more than once.
    protected fun setScreenContent(content: @Composable () -> Unit) {
        val client = SwrCachePlus(CoroutineScope(SupervisorJob() + Dispatchers.Unconfined))
        val snackbarHostState = SnackbarHostState()

        composeUiTest.setContent {
            SwrClientProvider(client = client) {
                CompositionLocalProvider(
                    LocalSnackbarHostState provides snackbarHostState,
                    LocalSafeClickInvoker provides SafeClickInvoker(interval = Duration.ZERO),
                    content = content,
                )
            }
        }
        composeUiTest.waitForIdle()
    }
}

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
