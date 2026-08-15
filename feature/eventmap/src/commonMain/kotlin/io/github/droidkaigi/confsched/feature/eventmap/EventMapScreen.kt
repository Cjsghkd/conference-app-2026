package io.github.droidkaigi.confsched.feature.eventmap

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.LocalePreviews
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.KaigiTopAppBar
import io.github.droidkaigi.confsched.feature.eventmap.component.FloorTabRow
import io.github.droidkaigi.confsched.feature.eventmap.component.SketchCard
import io.github.droidkaigi.confsched.feature.eventmap.component.StampRallyCard
import io.github.droidkaigi.confsched.feature.eventmap.generated.resources.Res
import io.github.droidkaigi.confsched.feature.eventmap.generated.resources.event_map_1f
import io.github.droidkaigi.confsched.feature.eventmap.generated.resources.event_map_b1f
import io.github.droidkaigi.confsched.feature.eventmap.generated.resources.event_map_content_description
import io.github.droidkaigi.confsched.feature.eventmap.generated.resources.event_map_introducing
import io.github.droidkaigi.confsched.feature.eventmap.generated.resources.event_map_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EventMapScreen(
    uiState: EventMapScreenUiState,
    onFloorClick: (EventMapFloor) -> Unit,
) {
    Scaffold(
        topBar = {
            KaigiTopAppBar(title = stringResource(Res.string.event_map_title))
        },
        contentWindowInsets = WindowInsets(),
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.event_map_introducing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FloorTabRow(
                selectedFloor = uiState.selectedFloor,
                onFloorClick = onFloorClick,
            )
            Crossfade(targetState = uiState.selectedFloor) { floor ->
                SketchCard {
                    Image(
                        painter = painterResource(floor.mapImage()),
                        contentDescription = stringResource(Res.string.event_map_content_description, floor.label),
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                    )
                }
            }
            StampRallyCard(
                onLearnMoreClick = { /*TODO*/ },
            )
        }
    }
}

private fun EventMapFloor.mapImage(): DrawableResource = when (this) {
    EventMapFloor.Ground -> Res.drawable.event_map_1f
    EventMapFloor.Basement -> Res.drawable.event_map_b1f
}

@LocalePreviews
@Composable
private fun EventMapScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        EventMapScreen(
            uiState = EventMapScreenUiState(selectedFloor = EventMapFloor.Ground),
            onFloorClick = {},
        )
    }
}
