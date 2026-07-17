package io.github.droidkaigi.confsched.core.model

import kotlinx.collections.immutable.PersistentSet
import soil.query.SubscriptionKey

typealias FavoriteTimetableIdsSubscriptionKey = SubscriptionKey<PersistentSet<TimetableItemId>>
