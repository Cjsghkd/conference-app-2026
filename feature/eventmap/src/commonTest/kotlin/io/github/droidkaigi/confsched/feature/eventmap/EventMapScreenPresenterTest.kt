package io.github.droidkaigi.confsched.feature.eventmap

import io.github.droidkaigi.confsched.core.testing.runPresenterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EventMapScreenPresenterTest {

    @Test
    fun initial_state_defaults_to_ground_floor_and_selecting_a_floor_updates_state() {
        runPresenterTest(
            presenterContext = EventMapPresenterContext(),
            presenter = { channel -> eventMapScreenPresenter(screenChannel = channel) },
        ) {
            val initial = uiStates.awaitItem()
            assertEquals(EventMapFloor.Ground, initial.selectedFloor)

            send(EventMapScreenAction.SelectFloor(EventMapFloor.Basement))
            val onBasement = uiStates.awaitItem()
            assertEquals(EventMapFloor.Basement, onBasement.selectedFloor)
        }
    }
}
