package io.github.droidkaigi.confsched.feature.sponsors.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.droidkaigi.confsched.core.model.Sponsor
import io.github.droidkaigi.confsched.core.model.SponsorGroup
import io.github.droidkaigi.confsched.core.model.SponsorPlan

internal fun LazyGridScope.sponsorPlanSection(
    group: SponsorGroup,
    onSponsorClick: (String) -> Unit,
) {
    item(key = group.plan.name, span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = group.plan.sectionTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
    }
    items(
        items = group.sponsors,
        key = Sponsor::name,
        span = { GridItemSpan(maxLineSpan / group.plan.itemsPerRow) },
    ) { sponsor ->
        SponsorItem(
            sponsor = sponsor,
            onSponsorClick = onSponsorClick,
            modifier = Modifier.fillMaxWidth().height(group.plan.itemHeight),
        )
    }
}

private val SponsorPlan.sectionTitle: String
    get() = when (this) {
        SponsorPlan.Platinum -> "Platinum Sponsors"
        SponsorPlan.Gold -> "Gold Sponsors"
        SponsorPlan.Supporter -> "Supporters"
    }

// Provisional proportions until the sponsor wall design lands; each value must divide the grid's column count.
private val SponsorPlan.itemsPerRow: Int
    get() = when (this) {
        SponsorPlan.Platinum -> 1
        SponsorPlan.Gold -> 2
        SponsorPlan.Supporter -> 3
    }

private val SponsorPlan.itemHeight: Dp
    get() = when (this) {
        SponsorPlan.Platinum -> 110.dp
        SponsorPlan.Gold -> 88.dp
        SponsorPlan.Supporter -> 72.dp
    }
