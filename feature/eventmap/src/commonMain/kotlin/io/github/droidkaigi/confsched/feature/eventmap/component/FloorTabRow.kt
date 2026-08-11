package io.github.droidkaigi.confsched.feature.eventmap.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.feature.eventmap.EventMapFloor

@Composable
internal fun FloorTabRow(
    selectedFloor: EventMapFloor,
    onFloorClick: (EventMapFloor) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        SingleChoiceSegmentedButtonRow {
            EventMapFloor.entries.forEachIndexed { index, floor ->
                SegmentedButton(
                    selected = selectedFloor == floor,
                    onClick = { onFloorClick(floor) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = EventMapFloor.entries.size),
                    modifier = Modifier.width(FloorTabRowDefaults.tabWidth),
                ) {
                    Text(floor.label)
                }
            }
        }
    }
}

private object FloorTabRowDefaults {
    val tabWidth = 104.dp
}

@Preview
@Composable
fun FloorTabRowPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        FloorTabRow(selectedFloor = EventMapFloor.entries.first(), onFloorClick = {})
    }
}
