package io.github.droidkaigi.confsched.feature.sessions.timetable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
internal fun TimetableItemDetailHeadline(
    item: TimetableItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = item.room.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )
        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                    .clip(CircleShape)
                    .size(52.dp)
                    .padding(14.dp),
            )
            Text(
                text = item.speaker,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview
@Composable
fun TimetableItemDetailHeadlinePreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        TimetableItemDetailHeadline(
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
