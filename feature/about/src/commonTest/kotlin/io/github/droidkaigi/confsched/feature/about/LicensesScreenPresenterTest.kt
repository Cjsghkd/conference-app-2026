package io.github.droidkaigi.confsched.feature.about

import dev.zacsweers.metro.createGraph
import io.github.droidkaigi.confsched.core.testing.runPresenterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LicensesScreenPresenterTest {

    private val graph = createGraph<LicensesScreenTestGraph>()

    private val sampleLibs = LicensesScreenUiState.fake().libs

    @Test
    fun the_collected_libraries_reach_the_ui_state() {
        runPresenterTest<LicensesPresenterContext, Unit, Unit, LicensesScreenUiState>(
            presenterContext = graph.presenterContext,
            presenter = { _ -> licensesScreenPresenter(sampleLibs) },
        ) {
            assertEquals(sampleLibs, uiStates.awaitItem().libs)
        }
    }
}
