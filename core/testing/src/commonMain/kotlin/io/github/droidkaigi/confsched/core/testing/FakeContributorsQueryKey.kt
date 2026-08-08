package io.github.droidkaigi.confsched.core.testing

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.droidkaigi.confsched.core.model.Contributors
import io.github.droidkaigi.confsched.core.model.ContributorsQueryKey
import kotlinx.collections.immutable.persistentListOf
import soil.query.QueryId
import soil.query.buildQueryKey

@SingleIn(TestingScope::class)
@ContributesBinding(TestingScope::class, binding = binding<ContributorsQueryKey>())
class FakeContributorsQueryKey private constructor(
    fixture: FakeFixture<Contributors>,
) : FakeKeyControl<Contributors>(fixture),
    ContributorsQueryKey by buildQueryKey(
        id = QueryId("fake-contributors"),
        fetch = { fixture.await() },
    ) {
    @Inject
    constructor() : this(FakeFixture(Contributors(items = persistentListOf())))
}
