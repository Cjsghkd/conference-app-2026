package io.github.droidkaigi.confsched.feature.staff

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.LocalePreviews
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme

@Composable
fun StaffScreen(
    uiState: StaffScreenUiState,
    onBackClick: () -> Unit,
) {
    Column {
        uiState.staff.forEach {
            Text(text = it.username, modifier = Modifier.padding(8.dp))
        }
    }
}

@LocalePreviews
@Composable
fun StaffScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        StaffScreen(
            uiState = StaffScreenUiState.fake(),
            onBackClick = {},
        )
    }
}
