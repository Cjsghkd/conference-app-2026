package io.github.droidkaigi.confsched.feature.about

import androidx.compose.foundation.layout.fillMaxSize
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
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.safeClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    uiState: LicensesScreenUiState,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Licenses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = safeClick(onBackClick)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        // AboutLibraries renders the rows, the inline detail and the license dialog, and opens
        // every link it offers through LocalUriHandler.
        LibrariesContainer(
            libraries = uiState.libs,
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
        )
    }
}

@Preview
@Composable
fun LicensesScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        LicensesScreen(
            uiState = LicensesScreenUiState.fake(),
            onBackClick = {},
        )
    }
}
