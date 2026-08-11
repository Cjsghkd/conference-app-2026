package io.github.droidkaigi.confsched.feature.profilecard

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.feature.profilecard.generated.resources.Res
import io.github.droidkaigi.confsched.feature.profilecard.generated.resources.profile_card
import org.jetbrains.compose.resources.stringResource

@Composable
context(_: ProfileCardPresenterContext)
fun profileCardScreenPresenter(): ProfileCardScreenUiState {
    return ProfileCardScreenUiState(
        title = stringResource(Res.string.profile_card),
    )
}
