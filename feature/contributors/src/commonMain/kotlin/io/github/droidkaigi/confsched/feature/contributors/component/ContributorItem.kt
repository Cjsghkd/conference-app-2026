package io.github.droidkaigi.confsched.feature.contributors.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.Contributor
import io.github.droidkaigi.confsched.core.ui.RemoteImage
import io.github.droidkaigi.confsched.core.ui.safeClickable

@Composable
internal fun ContributorItem(
    contributor: Contributor,
    onContributorClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .safeClickable { onContributorClick(contributor.profileUrl) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RemoteImage(
            imageUrl = contributor.iconUrl,
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = CircleShape),
        )
        Text(
            text = contributor.username,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
