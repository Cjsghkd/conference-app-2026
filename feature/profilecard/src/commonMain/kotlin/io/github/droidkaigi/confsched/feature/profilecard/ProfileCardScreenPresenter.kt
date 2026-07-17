package io.github.droidkaigi.confsched.feature.profilecard

import androidx.compose.runtime.Composable

context(_: ProfileCardPresenterContext)
@Composable
fun profileCardScreenPresenter(): ProfileCardScreenUiState {
    return ProfileCardScreenUiState(
        title = "Profile card",
    )
}
