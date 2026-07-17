package io.github.droidkaigi.confsched.feature.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.core.preview.PreviewImage
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewWrapper
import io.github.droidkaigi.confsched.core.ui.RemoteImage
import io.github.droidkaigi.confsched.core.ui.safeClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    uiState: AboutScreenUiState,
    onOpenFeaturedSession: (TimetableItemId) -> Unit,
    isDebugMenuAvailable: Boolean,
    onOpenDebug: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(uiState.title) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            RemoteImage(
                imageUrl = PreviewImage.SessionCover.imageUrl,
                contentDescription = null,
            )
            ListItem(
                headlineContent = { Text("Version") },
                trailingContent = { Text(uiState.versionName) },
            )
            HorizontalDivider()
            ListItem(
                modifier = Modifier.safeClickable { onOpenFeaturedSession(uiState.featuredSessionId) },
                headlineContent = { Text("Open featured session detail") },
                supportingContent = {
                    Text("Navigates to the sessions feature through AboutScreenNavigator")
                },
            )
            if (isDebugMenuAvailable) {
                HorizontalDivider()
                ListItem(
                    modifier = Modifier.safeClickable { onOpenDebug() },
                    headlineContent = { Text("Debug menu") },
                    supportingContent = { Text("Developer tools (debug builds only)") },
                )
            }
        }
    }
}

@PreviewWrapper(KaigiPreviewWrapper::class)
@Preview
@Composable
fun AboutScreenPreview() {
    AboutScreen(
        uiState = AboutScreenUiState(
            title = "About DroidKaigi 2026",
            versionName = "1.0.0",
            featuredSessionId = TimetableItemId("s6"),
        ),
        onOpenFeaturedSession = {},
        isDebugMenuAvailable = true,
        onOpenDebug = {},
    )
}

@PreviewWrapper(KaigiPreviewWrapper::class)
@Preview
@Composable
fun AboutScreenWithoutDebugMenuPreview() {
    AboutScreen(
        uiState = AboutScreenUiState(
            title = "About DroidKaigi 2026",
            versionName = "1.0.0",
            featuredSessionId = TimetableItemId("s6"),
        ),
        onOpenFeaturedSession = {},
        isDebugMenuAvailable = false,
        onOpenDebug = {},
    )
}
