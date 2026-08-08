package io.github.droidkaigi.confsched.core.testing

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.droidkaigi.confsched.core.model.FavoriteTimetableIdsSubscriptionKey
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.flow
import soil.query.SubscriptionId
import soil.query.buildSubscriptionKey

@SingleIn(TestingScope::class)
@ContributesBinding(TestingScope::class, binding = binding<FavoriteTimetableIdsSubscriptionKey>())
class FakeFavoriteTimetableIdsSubscriptionKey private constructor(
    fixture: FakeFixture<PersistentSet<TimetableItemId>>,
) : FakeKeyControl<PersistentSet<TimetableItemId>>(fixture),
    FavoriteTimetableIdsSubscriptionKey by buildSubscriptionKey(
        id = SubscriptionId("fake-favorite-ids"),
        subscribe = { flow { emit(fixture.await()) } },
    ) {
    @Inject
    constructor() : this(FakeFixture(persistentSetOf()))
}
