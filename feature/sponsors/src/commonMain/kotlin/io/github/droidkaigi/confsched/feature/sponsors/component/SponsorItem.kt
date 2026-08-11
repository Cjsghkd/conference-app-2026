package io.github.droidkaigi.confsched.feature.sponsors.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider
import io.github.droidkaigi.confsched.core.preview.PreviewImage
import io.github.droidkaigi.confsched.core.preview.wrapper.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.ui.RemoteImage

@Composable
internal fun SponsorItem(
    name: String,
    logoUrl: String,
    onSponsorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(role = Role.Button, onClick = onSponsorClick),
        // Sponsor logos are supplied for a white background, so the card does not follow the theme.
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        RemoteImage(
            imageUrl = logoUrl,
            contentDescription = name,
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
            // Logos arrive in arbitrary aspect ratios; cropping one to the card would cut off the wordmark.
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview
@Composable
fun SponsorItemPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        SponsorItem(
            name = "Sponsor A",
            logoUrl = PreviewImage.SessionCover.imageUrl,
            onSponsorClick = {},
        )
    }
}
