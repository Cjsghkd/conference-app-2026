package io.github.droidkaigi.confsched.feature.sponsors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.feature.sponsors.component.SPONSOR_GRID_COLUMNS
import io.github.droidkaigi.confsched.feature.sponsors.component.SponsorsEmptyView
import io.github.droidkaigi.confsched.feature.sponsors.component.sponsorPlanSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorsScreen(
    uiState: SponsorsScreenUiState,
    onSponsorClick: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sponsors", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.groups.isEmpty()) {
            SponsorsEmptyView(modifier = Modifier.padding(innerPadding))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(SPONSOR_GRID_COLUMNS),
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.groups.forEach { group ->
                    sponsorPlanSection(group = group, onSponsorClick = onSponsorClick)
                }
            }
        }
    }
}

@Preview
@Composable
fun SponsorsScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        SponsorsScreen(
            uiState = SponsorsScreenUiState.fake(),
            onSponsorClick = {},
            onBackClick = {},
        )
    }
}
