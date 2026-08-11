package io.github.droidkaigi.confsched.core.testing

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.droidkaigi.confsched.core.model.Staff
import io.github.droidkaigi.confsched.core.model.StaffQueryKey
import kotlinx.collections.immutable.persistentListOf
import soil.query.QueryId
import soil.query.buildQueryKey

@SingleIn(TestingScope::class)
@ContributesBinding(TestingScope::class, binding = binding<StaffQueryKey>())
class FakeStaffQueryKey private constructor(
    fixture: FakeFixture<Staff>,
) : FakeKeyControl<Staff>(fixture),
    StaffQueryKey by buildQueryKey(
        id = QueryId("fake-staff"),
        fetch = { fixture.await() },
    ) {
    @Inject
    constructor() : this(FakeFixture(Staff(items = persistentListOf())))
}
