package io.github.droidkaigi.confsched.core.testing

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.droidkaigi.confsched.core.model.Sponsors
import io.github.droidkaigi.confsched.core.model.SponsorsQueryKey
import kotlinx.collections.immutable.persistentListOf
import soil.query.QueryId
import soil.query.buildQueryKey

@SingleIn(TestingScope::class)
@ContributesBinding(TestingScope::class, binding = binding<SponsorsQueryKey>())
class FakeSponsorsQueryKey private constructor(
    fixture: FakeFixture<Sponsors>,
) : FakeKeyControl<Sponsors>(fixture),
    SponsorsQueryKey by buildQueryKey(
        id = QueryId("fake-sponsors"),
        fetch = { fixture.await() },
    ) {
    @Inject
    constructor() : this(FakeFixture(Sponsors(groups = persistentListOf())))
}
