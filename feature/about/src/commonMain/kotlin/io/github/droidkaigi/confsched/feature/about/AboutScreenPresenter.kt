package io.github.droidkaigi.confsched.feature.about

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.feature.about.generated.resources.Res
import io.github.droidkaigi.confsched.feature.about.generated.resources.about_title
import org.jetbrains.compose.resources.stringResource

@Composable
context(presenterContext: AboutPresenterContext)
fun aboutScreenPresenter(): AboutScreenUiState {
    return AboutScreenUiState(
        title = stringResource(Res.string.about_title),
        versionName = presenterContext.buildConfig.versionName,
    )
}
