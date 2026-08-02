package io.github.droidkaigi.confsched.feature.sponsors

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.droidkaigi.confsched.core.common.LocalSafeClickInvoker
import io.github.droidkaigi.confsched.core.common.SafeClickInvoker
import io.github.droidkaigi.confsched.core.common.context
import io.github.droidkaigi.confsched.core.model.Sponsors
import io.github.droidkaigi.confsched.core.model.SponsorsQueryKey
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
class SponsorsScreenRobot(composeUiTest: ComposeUiTest) : Robot(composeUiTest) {

    private val openedSites = mutableListOf<String>()

    fun setupContent(sponsors: Sponsors) {
        val queryKey: SponsorsQueryKey = buildQueryKey(
            id = QueryId("robot-sponsors"),
            fetch = { sponsors },
        )
        val screenContext = SponsorsScreenContext(
            sponsorsQueryKey = queryKey,
            presenterContext = SponsorsPresenterContext(),
        )
        val client = SwrCachePlus(CoroutineScope(SupervisorJob() + Dispatchers.Unconfined))

        composeUiTest.setContent {
            SwrClientProvider(client = client) {
                CompositionLocalProvider(
                    LocalSafeClickInvoker provides SafeClickInvoker(interval = Duration.ZERO),
                ) {
                    context(screenContext) {
                        SponsorsScreenRoot(
                            onNavigateBack = {},
                            onNavigateToSponsorSite = openedSites::add,
                        )
                    }
                }
            }
        }
        composeUiTest.waitForIdle()
    }

    fun clickSponsor(name: String) {
        composeUiTest.onNodeWithContentDescription(name).performClick()
        composeUiTest.waitForIdle()
    }

    fun checkPlanSectionDisplayed(title: String) {
        composeUiTest.onNodeWithText(title).assertIsDisplayed()
    }

    fun checkSponsorDisplayed(name: String) {
        composeUiTest.onNodeWithContentDescription(name).assertIsDisplayed()
    }

    fun checkPlanSectionDoesNotExist(title: String) {
        composeUiTest.onNodeWithText(title).assertDoesNotExist()
    }

    fun checkOpenedSites(vararg links: String) {
        assertEquals(links.toList(), openedSites)
    }
}
