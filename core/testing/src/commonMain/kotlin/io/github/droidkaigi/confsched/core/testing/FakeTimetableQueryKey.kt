package io.github.droidkaigi.confsched.core.testing

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.droidkaigi.confsched.core.model.Timetable
import io.github.droidkaigi.confsched.core.model.TimetableQueryKey
import kotlinx.collections.immutable.persistentListOf
import soil.query.QueryId
import soil.query.buildQueryKey

@SingleIn(TestingScope::class)
@ContributesBinding(TestingScope::class, binding = binding<TimetableQueryKey>())
class FakeTimetableQueryKey private constructor(
    fixture: FakeFixture<Timetable>,
) : FakeKeyControl<Timetable>(fixture),
    TimetableQueryKey by buildQueryKey(
        id = QueryId("fake-timetable"),
        fetch = { fixture.await() },
    ) {
    @Inject
    constructor() : this(FakeFixture(Timetable(items = persistentListOf())))
}
