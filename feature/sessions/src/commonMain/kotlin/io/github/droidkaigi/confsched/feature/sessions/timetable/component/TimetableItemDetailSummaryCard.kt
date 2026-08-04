package io.github.droidkaigi.confsched.feature.sessions.timetable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme

@Composable
internal fun TimetableItemDetailSummaryCard(
    item: TimetableItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryRow(
            imageVector = Icons.Outlined.Schedule,
            title = "Schedule",
            description = "${item.day.name} ${item.startsAt} - ${item.endsAt}",
        )
        SummaryRow(
            imageVector = Icons.Outlined.LocationOn,
            title = "Location",
            description = item.room,
        )
    }
}

@Composable
private fun SummaryRow(
    imageVector: ImageVector,
    title: String,
    description: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview
@Composable
fun TimetableItemDetailSummaryCardPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        TimetableItemDetailSummaryCard(
            item = TimetableItem(
                id = TimetableItemId("d1a"),
                title = "Compose Multiplatform in Practice",
                room = "Arctic Fox",
                speaker = "Alice",
                day = DroidKaigi2026Day.Day1,
                startsAt = "10:00",
                endsAt = "10:40",
            ),
        )
    }
}
