package io.github.droidkaigi.confsched.feature.staff

import io.github.droidkaigi.confsched.core.model.Staff
import io.github.droidkaigi.confsched.core.model.StaffMember
import io.github.droidkaigi.confsched.core.preview.fake
import kotlinx.collections.immutable.PersistentList

data class StaffScreenUiState(
    val staff: PersistentList<StaffMember>,
) {
    companion object
}

internal fun StaffScreenUiState.Companion.fake(): StaffScreenUiState = StaffScreenUiState(
    staff = Staff.fake().items,
)
