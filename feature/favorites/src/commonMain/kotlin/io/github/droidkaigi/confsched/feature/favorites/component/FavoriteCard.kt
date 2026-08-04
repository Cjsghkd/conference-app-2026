package io.github.droidkaigi.confsched.feature.favorites.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.safeClick
import io.github.droidkaigi.confsched.core.ui.safeClickable

@Composable
internal fun FavoriteCard(
    item: TimetableItem,
    onBookmarkClick: (TimetableItemId) -> Unit,
    onClick: (TimetableItemId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().safeClickable { onClick(item.id) }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.day.name, style = MaterialTheme.typography.labelSmall)
                Text(item.title, fontWeight = FontWeight.Bold)
                Text("${item.room} · ${item.speaker}")
            }
            IconButton(onClick = safeClick { onBookmarkClick(item.id) }) {
                Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Remove favorite")
            }
        }
    }
}

@Preview
@Composable
fun FavoriteCardPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        FavoriteCard(
            item = TimetableItem(
                id = TimetableItemId("d1a"),
                title = "Compose Multiplatform in Practice",
                room = "Arctic Fox",
                speaker = "Sp1",
                day = DroidKaigi2026Day.Day1,
                startsAt = "10:00",
                endsAt = "10:40",
            ),
            onBookmarkClick = {},
            onClick = {},
        )
    }
}
