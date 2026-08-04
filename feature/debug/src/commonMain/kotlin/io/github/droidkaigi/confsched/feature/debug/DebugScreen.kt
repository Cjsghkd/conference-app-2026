package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.safeClick
import io.github.droidkaigi.confsched.core.ui.safeClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    uiState: DebugScreenUiState,
    // Not named on* so the safeClick checker does not treat it as a navigation callback.
    toggleSoilErrorOverlay: (Boolean) -> Unit,
    onOpenSoilErrors: () -> Unit,
    onClearData: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug menu") },
                navigationIcon = {
                    IconButton(onClick = safeClick(onBack)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("App")
            ListItem(
                headlineContent = { Text("Version") },
                trailingContent = { Text(uiState.appVersion) },
            )
            HorizontalDivider()

            SectionHeader("Soil")
            ListItem(
                headlineContent = { Text("Show Soil error sheet") },
                supportingContent = { Text("Pop up a bottom sheet whenever a query, mutation, or subscription fails") },
                trailingContent = {
                    Switch(
                        checked = uiState.soilErrorOverlayEnabled,
                        onCheckedChange = toggleSoilErrorOverlay,
                    )
                },
            )
            ListItem(
                modifier = Modifier.safeClickable(onClick = onOpenSoilErrors),
                headlineContent = { Text("Soil errors") },
                supportingContent = { Text("Errors relayed during this session") },
                trailingContent = { Text("${uiState.soilErrors.size}") },
            )
            HorizontalDivider()

            SectionHeader("Data")
            ListItem(
                modifier = Modifier.safeClickable(onClick = onClearData),
                headlineContent = { Text("Clear persisted data") },
                supportingContent = {
                    Text(
                        if (uiState.dataCleared) {
                            "Persisted data cleared ✓"
                        } else {
                            "Removes settings, favorites, and cached responses"
                        },
                    )
                },
            )
        }
    }
}

@Preview
@Composable
fun DebugScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        DebugScreen(
            uiState = DebugScreenUiState(
                appVersion = "0.1.0",
                dataCleared = false,
                soilErrorOverlayEnabled = true,
                soilErrors = listOf(previewSoilError()),
            ),
            toggleSoilErrorOverlay = {},
            onOpenSoilErrors = {},
            onClearData = {},
            onBack = {},
        )
    }
}
