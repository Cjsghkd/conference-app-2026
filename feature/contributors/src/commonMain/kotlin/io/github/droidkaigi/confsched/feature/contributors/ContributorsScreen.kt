package io.github.droidkaigi.confsched.feature.contributors

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.droidkaigi.confsched.core.model.Contributor
import io.github.droidkaigi.confsched.core.model.ContributorId
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.PreviewImage
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.safeClick
import io.github.droidkaigi.confsched.feature.contributors.component.ContributorItem
import io.github.droidkaigi.confsched.feature.contributors.component.ContributorsCountText
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributorsScreen(
    uiState: ContributorsScreenUiState,
    onContributorClick: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contributors", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = safeClick(onBackClick)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            item(key = "count") {
                ContributorsCountText(count = uiState.contributors.size)
            }
            items(items = uiState.contributors, key = { it.id.value }) { contributor ->
                ContributorItem(contributor = contributor, onContributorClick = onContributorClick)
            }
        }
    }
}

private fun previewUiState() = ContributorsScreenUiState(
    contributors = persistentListOf(
        previewContributor(1L, "alice"),
        previewContributor(2L, "bob"),
        previewContributor(3L, "carol"),
    ),
)

private fun previewContributor(id: Long, username: String) = Contributor(
    id = ContributorId(id),
    username = username,
    iconUrl = PreviewImage.SpeakerAvatarA.imageUrl,
    profileUrl = "https://github.com/$username",
)

@Preview
@Composable
fun ContributorsScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        ContributorsScreen(
            uiState = previewUiState(),
            onContributorClick = {},
            onBackClick = {},
        )
    }
}
