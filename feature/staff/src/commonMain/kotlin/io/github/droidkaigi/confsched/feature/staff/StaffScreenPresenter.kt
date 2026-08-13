package io.github.droidkaigi.confsched.feature.staff

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.model.Staff

@Composable
context(_: StaffPresenterContext)
fun staffScreenPresenter(staff: Staff): StaffScreenUiState {
    return StaffScreenUiState(staff = staff.items)
}
