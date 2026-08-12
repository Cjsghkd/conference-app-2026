package io.github.droidkaigi.confsched.feature.staff

import dev.zacsweers.metro.createGraph
import io.github.droidkaigi.confsched.core.model.Staff
import io.github.droidkaigi.confsched.core.model.StaffId
import io.github.droidkaigi.confsched.core.model.StaffMember
import io.github.droidkaigi.confsched.core.testing.runPresenterTest
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class StaffScreenPresenterTest {

    private val graph = createGraph<StaffScreenTestGraph>()

    private val sampleStaff = Staff(
        items = persistentListOf(
            staffMember(2L, "staff-b"),
            staffMember(1L, "staff-a"),
        ),
    )

    @Test
    fun the_loaded_staff_reach_the_ui_state_in_order() {
        runPresenterTest<StaffPresenterContext, Unit, Unit, StaffScreenUiState>(
            presenterContext = graph.presenterContext,
            presenter = { _ -> staffScreenPresenter(sampleStaff) },
        ) {
            val staff = uiStates.awaitItem().staff
            assertEquals(sampleStaff.items, staff)
            assertEquals(listOf("staff-b", "staff-a"), staff.map { it.username })
        }
    }

    private fun staffMember(id: Long, username: String) = StaffMember(
        id = StaffId(id),
        username = username,
        iconUrl = "https://example.com/$username.png",
        profileUrl = "https://example.com/$username",
    )
}
