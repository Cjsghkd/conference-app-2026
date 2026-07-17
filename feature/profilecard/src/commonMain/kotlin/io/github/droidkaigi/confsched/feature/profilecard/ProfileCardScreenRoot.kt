package io.github.droidkaigi.confsched.feature.profilecard

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context

context(screenContext: ProfileCardScreenContext)
@Composable
fun ProfileCardScreenRoot() {
    val uiState = context(screenContext.presenterContext) {
        profileCardScreenPresenter()
    }
    ProfileCardScreen(uiState = uiState)
}
