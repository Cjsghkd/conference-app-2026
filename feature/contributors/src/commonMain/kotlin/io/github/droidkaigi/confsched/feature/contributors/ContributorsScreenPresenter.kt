package io.github.droidkaigi.confsched.feature.contributors

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.model.Contributors

@Composable
context(_: ContributorsPresenterContext)
fun contributorsScreenPresenter(contributors: Contributors): ContributorsScreenUiState {
    return ContributorsScreenUiState(contributors = contributors.items)
}
