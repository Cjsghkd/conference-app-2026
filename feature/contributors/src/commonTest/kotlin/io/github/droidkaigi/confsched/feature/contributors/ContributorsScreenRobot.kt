package io.github.droidkaigi.confsched.feature.contributors

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.droidkaigi.confsched.core.common.LocalSafeClickInvoker
import io.github.droidkaigi.confsched.core.common.SafeClickInvoker
import io.github.droidkaigi.confsched.core.common.context
import io.github.droidkaigi.confsched.core.model.Contributors
import io.github.droidkaigi.confsched.core.model.ContributorsQueryKey
import io.github.droidkaigi.confsched.core.testing.Robot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import soil.query.QueryId
import soil.query.SwrCachePlus
import soil.query.buildQueryKey
import soil.query.compose.SwrClientProvider
import kotlin.test.assertEquals
import kotlin.time.Duration

@OptIn(ExperimentalTestApi::class)
class ContributorsScreenRobot(composeUiTest: ComposeUiTest) : Robot(composeUiTest) {

    private val openedProfiles = mutableListOf<String>()

    fun setupContent(contributors: Contributors) {
        val queryKey: ContributorsQueryKey = buildQueryKey(
            id = QueryId("robot-contributors"),
            fetch = { contributors },
        )
        val screenContext = ContributorsScreenContext(
            contributorsQueryKey = queryKey,
            presenterContext = ContributorsPresenterContext(),
        )
        val client = SwrCachePlus(CoroutineScope(SupervisorJob() + Dispatchers.Unconfined))

        composeUiTest.setContent {
            SwrClientProvider(client = client) {
                CompositionLocalProvider(
                    LocalSafeClickInvoker provides SafeClickInvoker(interval = Duration.ZERO),
                ) {
                    context(screenContext) {
                        ContributorsScreenRoot(
                            onNavigateBack = {},
                            onNavigateToContributorProfile = openedProfiles::add,
                        )
                    }
                }
            }
        }
        composeUiTest.waitForIdle()
    }

    fun clickContributor(username: String) {
        composeUiTest.onNodeWithText(username).performClick()
        composeUiTest.waitForIdle()
    }

    fun checkContributorDisplayed(username: String) {
        composeUiTest.onNodeWithText(username).assertIsDisplayed()
    }

    fun checkCountDisplayed(count: Int) {
        composeUiTest.onNodeWithText("$count contributors").assertIsDisplayed()
    }

    fun checkOpenedProfiles(vararg urls: String) {
        assertEquals(urls.toList(), openedProfiles)
    }
}
