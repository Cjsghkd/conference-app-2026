package io.github.droidkaigi.confsched.feature.contributors

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.LocalePreviews
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.KaigiLargeTopAppBar
import io.github.droidkaigi.confsched.feature.contributors.component.ContributorItem
import io.github.droidkaigi.confsched.feature.contributors.component.ContributorsCountText
import io.github.droidkaigi.confsched.feature.contributors.component.ContributorsEmptyView
import io.github.droidkaigi.confsched.feature.contributors.generated.resources.Res
import io.github.droidkaigi.confsched.feature.contributors.generated.resources.contributors
import org.jetbrains.compose.resources.stringResource

@Composable
fun ContributorsScreen(
    uiState: ContributorsScreenUiState,
    onContributorClick: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            KaigiLargeTopAppBar(title = stringResource(Res.string.contributors), onBackClick = onBackClick)
        },
    ) { innerPadding ->
        if (uiState.contributors.isEmpty()) {
            ContributorsEmptyView(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                item(key = "count") {
                    ContributorsCountText(count = uiState.contributors.size)
                }
                items(items = uiState.contributors, key = { it.id.value }) { contributor ->
                    ContributorItem(
                        username = contributor.username,
                        iconUrl = contributor.iconUrl,
                        onContributorClick = { onContributorClick(contributor.profileUrl) },
                    )
                }
            }
        }
    }
}

@LocalePreviews
@Composable
fun ContributorsScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        ContributorsScreen(
            uiState = ContributorsScreenUiState.fake(),
            onContributorClick = {},
            onBackClick = {},
        )
    }
}
