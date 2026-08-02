package io.github.droidkaigi.confsched.feature.sponsors.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.Sponsor
import io.github.droidkaigi.confsched.core.ui.RemoteImage
import io.github.droidkaigi.confsched.core.ui.safeClickable

@Composable
internal fun SponsorItem(
    sponsor: Sponsor,
    onSponsorClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.safeClickable { onSponsorClick(sponsor.link) },
        // Sponsor logos are supplied for a white background, so the card does not follow the theme.
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        RemoteImage(
            imageUrl = sponsor.logoUrl,
            contentDescription = sponsor.name,
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}
