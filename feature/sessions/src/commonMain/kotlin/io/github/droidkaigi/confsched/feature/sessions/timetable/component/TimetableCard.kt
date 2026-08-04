package io.github.droidkaigi.confsched.feature.sessions.timetable.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewWrapper
import io.github.droidkaigi.confsched.core.ui.safeClick
import io.github.droidkaigi.confsched.core.ui.safeClickable

@Composable
internal fun TimetableCard(
    id: TimetableItemId,
    title: String,
    room: String,
    speaker: String,
    isFavorite: Boolean,
    onBookmarkClick: (TimetableItemId) -> Unit,
    onClick: (TimetableItemId) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().safeClickable { onClick(id) }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text("$room · $speaker")
            }
            IconButton(onClick = safeClick { onBookmarkClick(id) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
                )
            }
        }
    }
}

@PreviewWrapper(KaigiPreviewWrapper::class)
@Preview
@Composable
fun TimetableCardPreview() {
    TimetableCard(
        id = TimetableItemId("d1a"),
        title = "Compose Multiplatform in Practice",
        room = "Arctic Fox",
        speaker = "Alice",
        isFavorite = true,
        onBookmarkClick = {},
        onClick = {},
    )
}
