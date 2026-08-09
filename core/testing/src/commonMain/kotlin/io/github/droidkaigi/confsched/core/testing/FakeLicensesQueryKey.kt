package io.github.droidkaigi.confsched.core.testing

import com.mikepenz.aboutlibraries.Libs
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.droidkaigi.confsched.core.model.LicensesQueryKey
import soil.query.QueryId
import soil.query.buildQueryKey

@SingleIn(TestingScope::class)
@ContributesBinding(TestingScope::class, binding = binding<LicensesQueryKey>())
class FakeLicensesQueryKey private constructor(
    fixture: FakeFixture<Libs>,
) : FakeKeyControl<Libs>(fixture),
    LicensesQueryKey by buildQueryKey(
        id = QueryId("fake-licenses"),
        fetch = { fixture.await() },
    ) {
    @Inject
    constructor() : this(FakeFixture(Libs(libraries = emptyList(), licenses = emptySet())))
}
