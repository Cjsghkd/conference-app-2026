package io.github.droidkaigi.confsched.feature.sponsors.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.droidkaigi.confsched.core.model.SponsorGroup
import io.github.droidkaigi.confsched.core.model.SponsorPlan
import io.github.droidkaigi.confsched.feature.sponsors.generated.resources.Res
import io.github.droidkaigi.confsched.feature.sponsors.generated.resources.sponsor_plan_gold
import io.github.droidkaigi.confsched.feature.sponsors.generated.resources.sponsor_plan_platinum
import io.github.droidkaigi.confsched.feature.sponsors.generated.resources.sponsor_plan_supporter
import io.github.droidkaigi.confsched.feature.sponsors.generated.resources.sponsor_supporters_label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal const val SPONSOR_GRID_COLUMNS = 6

// Extra spacing to meet the design's required heading-to-content vertical gap.
private val HEADING_EXTRA_SPACING = 20.dp

// The frame all three plans' decorations share, per design: a fixed box so the title's vertical
// position stays put even though each plan's decoration artwork has a different visual footprint.
private val HEADING_WIDTH = 364.dp
private val HEADING_HEIGHT = 96.dp
private val HEADING_TITLE_TOP = 32.dp
private val HEADING_TITLE_HEIGHT = 32.dp

internal fun LazyGridScope.sponsorPlanSection(
    group: SponsorGroup,
    onSponsorClick: (String) -> Unit,
) {
    // The payload does not guarantee a unique sponsorName and a duplicate key throws in a lazy
    // layout, so this grid stays on positional keys.
    item(span = { GridItemSpan(maxLineSpan) }) {
        Box(
            modifier = Modifier
                .padding(top = group.plan.headingExtraTopSpacing, bottom = HEADING_EXTRA_SPACING)
                .size(HEADING_WIDTH, HEADING_HEIGHT),
        ) {
            // Decorative: the title text below carries the accessible label.
            SponsorHeadingDecoration(
                plan = group.plan,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = group.plan.headingDecorationTop),
            )

            Text(
                text = stringResource(group.plan.headingTitle),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    letterSpacing = 0.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = HEADING_TITLE_TOP)
                    .padding(horizontal = group.plan.headingTitleHorizontalPadding)
                    .size(width = group.plan.headingTitleWidth, height = HEADING_TITLE_HEIGHT),
            )
        }
    }
    if (group.plan == SponsorPlan.Supporter) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = stringResource(Res.string.sponsor_supporters_label),
                maxLines = 1,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                    color = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
    items(
        items = group.sponsors,
        span = { GridItemSpan(group.plan.itemColumnSpan) },
    ) { sponsor ->
        SponsorItem(
            name = sponsor.name,
            logoUrl = sponsor.logoUrl,
            onSponsorClick = { onSponsorClick(sponsor.link) },
            shape = group.plan.itemShape,
            contentPadding = group.plan.itemContentPadding,
            modifier = Modifier.fillMaxWidth().height(group.plan.itemHeight),
        )
    }
}

private val SponsorPlan.itemColumnSpan: Int
    get() = when (this) {
        SponsorPlan.Platinum -> SPONSOR_GRID_COLUMNS
        SponsorPlan.Gold -> SPONSOR_GRID_COLUMNS / 2
        SponsorPlan.Supporter -> SPONSOR_GRID_COLUMNS / 3
    }

private val SponsorPlan.headingTitle: StringResource
    get() = when (this) {
        SponsorPlan.Platinum -> Res.string.sponsor_plan_platinum
        SponsorPlan.Gold -> Res.string.sponsor_plan_gold
        SponsorPlan.Supporter -> Res.string.sponsor_plan_supporter
    }

private val SponsorPlan.itemHeight: Dp
    get() = when (this) {
        SponsorPlan.Platinum -> 80.dp
        SponsorPlan.Gold -> 80.dp
        SponsorPlan.Supporter -> 60.dp
    }

private val SponsorPlan.itemShape: Shape
    get() = when (this) {
        SponsorPlan.Platinum -> RoundedCornerShape(16.dp)
        SponsorPlan.Gold -> RoundedCornerShape(12.dp)
        SponsorPlan.Supporter -> RoundedCornerShape(8.dp)
    }

private val SponsorPlan.itemContentPadding: PaddingValues
    get() = when (this) {
        SponsorPlan.Platinum -> PaddingValues(16.dp)
        SponsorPlan.Gold -> PaddingValues(12.dp)
        SponsorPlan.Supporter -> PaddingValues(8.dp)
    }

private val SponsorPlan.headingDecorationTop: Dp
    get() = when (this) {
        SponsorPlan.Platinum -> 10.dp
        SponsorPlan.Gold -> 15.dp
        SponsorPlan.Supporter -> 61.dp
    }

private val SponsorPlan.headingTitleWidth: Dp
    get() = when (this) {
        SponsorPlan.Platinum -> 251.dp
        SponsorPlan.Gold -> 191.dp
        SponsorPlan.Supporter -> 115.685.dp
    }

private val SponsorPlan.headingTitleHorizontalPadding: Dp
    get() = when (this) {
        SponsorPlan.Platinum -> 56.5.dp
        SponsorPlan.Gold -> 86.5.dp
        SponsorPlan.Supporter -> 124.16.dp
    }

// Extra top spacing applied before Gold and Supporter headings.
private val SponsorPlan.headingExtraTopSpacing: Dp
    get() = when (this) {
        SponsorPlan.Platinum -> 0.dp
        SponsorPlan.Gold -> HEADING_EXTRA_SPACING
        SponsorPlan.Supporter -> HEADING_EXTRA_SPACING
    }
