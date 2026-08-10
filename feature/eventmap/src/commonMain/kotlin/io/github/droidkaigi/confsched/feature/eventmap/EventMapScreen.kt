package io.github.droidkaigi.confsched.feature.eventmap

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import io.github.droidkaigi.confsched.feature.eventmap.component.FloorTabRow
import io.github.droidkaigi.confsched.feature.eventmap.generated.resources.Res
import io.github.droidkaigi.confsched.feature.eventmap.generated.resources.event_map_1f
import io.github.droidkaigi.confsched.feature.eventmap.generated.resources.event_map_b1f
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventMapScreen(
    uiState: EventMapScreenUiState,
    onFloorClick: (EventMapFloor) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Event Map", fontWeight = FontWeight.Bold) })
        },
        contentWindowInsets = WindowInsets(),
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            FloorTabRow(selectedFloor = uiState.selectedFloor, onFloorClick = onFloorClick)

            Crossfade(targetState = uiState.selectedFloor) { floor ->
                Image(
                    painter = painterResource(floor.mapImage()),
                    contentDescription = "Map of ${floor.label}",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                )
            }
        }
    }
}

private fun EventMapFloor.mapImage(): DrawableResource = when (this) {
    EventMapFloor.Ground -> Res.drawable.event_map_1f
    EventMapFloor.Basement -> Res.drawable.event_map_b1f
}

@Preview
@Composable
fun EventMapScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        EventMapScreen(
            uiState = EventMapScreenUiState(selectedFloor = EventMapFloor.Ground),
            onFloorClick = {},
        )
    }
}
