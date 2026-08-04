package io.github.droidkaigi.confsched.core.data

import io.github.droidkaigi.confsched.core.model.Contributor
import io.github.droidkaigi.confsched.core.model.ContributorId
import io.github.droidkaigi.confsched.core.model.Contributors
import kotlinx.collections.immutable.toPersistentList

fun ContributorListResponse.toContributors(): Contributors = Contributors(
    items = contributors.map(ContributorResponse::toContributor).toPersistentList(),
)

private fun ContributorResponse.toContributor(): Contributor = Contributor(
    id = ContributorId(id),
    username = username,
    // The payload carries four icon sizes; 128px is the largest and covers list avatars at every density.
    iconUrl = icon128Url,
    profileUrl = profileUrl,
)
