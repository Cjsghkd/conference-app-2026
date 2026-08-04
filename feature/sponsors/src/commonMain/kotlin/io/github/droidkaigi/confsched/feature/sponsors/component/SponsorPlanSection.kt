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
import io.github.droidkaigi.confsched.core.model.SponsorGroup
import io.github.droidkaigi.confsched.core.model.SponsorPlan

// The column count and the per-plan spans belong together: a span is a column count out of this total.
internal const val SPONSOR_GRID_COLUMNS = 6

internal fun LazyGridScope.sponsorPlanSection(
    group: SponsorGroup,
    onSponsorClick: (String) -> Unit,
) {
    // The payload does not guarantee a unique sponsorName and a duplicate key throws in a lazy
    // layout, so this grid stays on positional keys.
    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = group.plan.sectionTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
    }
    items(
        items = group.sponsors,
        span = { GridItemSpan(group.plan.columnSpan) },
    ) { sponsor ->
        SponsorItem(
            name = sponsor.name,
            logoUrl = sponsor.logoUrl,
            link = sponsor.link,
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

// Provisional proportions until the sponsor wall design lands.
private val SponsorPlan.columnSpan: Int
    get() = when (this) {
        SponsorPlan.Platinum -> SPONSOR_GRID_COLUMNS
        SponsorPlan.Gold -> 3
        SponsorPlan.Supporter -> 2
    }

private val SponsorPlan.itemHeight: Dp
    get() = when (this) {
        SponsorPlan.Platinum -> 110.dp
        SponsorPlan.Gold -> 88.dp
        SponsorPlan.Supporter -> 72.dp
    }
