package io.github.droidkaigi.confsched.feature.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.PreviewImage
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.KaigiTopAppBar
import io.github.droidkaigi.confsched.core.ui.RemoteImage

@Composable
fun AboutScreen(
    uiState: AboutScreenUiState,
    onOpenSponsors: () -> Unit,
    onOpenContributors: () -> Unit,
    onOpenLicenses: () -> Unit,
    isDebugMenuAvailable: Boolean,
    onOpenDebug: () -> Unit,
) {
    Scaffold(
        topBar = { KaigiTopAppBar(title = uiState.title) },
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
                modifier = Modifier.clickable(onClick = onOpenSponsors),
                headlineContent = { Text("Sponsors") },
            )
            HorizontalDivider()
            ListItem(
                modifier = Modifier.clickable(onClick = onOpenContributors),
                headlineContent = { Text("Contributors") },
            )
            HorizontalDivider()
            ListItem(
                modifier = Modifier.clickable(onClick = onOpenLicenses),
                headlineContent = { Text("Licenses") },
                supportingContent = { Text("Open source libraries this build depends on") },
            )
            if (isDebugMenuAvailable) {
                HorizontalDivider()
                ListItem(
                    modifier = Modifier.clickable(onClick = onOpenDebug),
                    headlineContent = { Text("Debug menu") },
                    supportingContent = { Text("Developer tools (debug builds only)") },
                )
            }
        }
    }
}

@Preview
@Composable
fun AboutScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        AboutScreen(
            uiState = AboutScreenUiState(
                title = "About DroidKaigi 2026",
                versionName = "1.0.0",
            ),
            onOpenSponsors = {},
            onOpenContributors = {},
            onOpenLicenses = {},
            isDebugMenuAvailable = true,
            onOpenDebug = {},
        )
    }
}

@Preview
@Composable
fun AboutScreenWithoutDebugMenuPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        AboutScreen(
            uiState = AboutScreenUiState(
                title = "About DroidKaigi 2026",
                versionName = "1.0.0",
            ),
            onOpenSponsors = {},
            onOpenContributors = {},
            onOpenLicenses = {},
            isDebugMenuAvailable = false,
            onOpenDebug = {},
        )
    }
}
