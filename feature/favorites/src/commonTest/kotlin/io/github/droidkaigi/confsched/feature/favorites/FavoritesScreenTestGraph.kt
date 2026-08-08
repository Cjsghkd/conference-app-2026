package io.github.droidkaigi.confsched.feature.favorites

import dev.zacsweers.metro.DependencyGraph
import io.github.droidkaigi.confsched.core.testing.FakeFavoriteTimetableItemIdMutationKey
import io.github.droidkaigi.confsched.core.testing.TestingScope

@DependencyGraph(scope = TestingScope::class)
interface FavoritesScreenTestGraph {
    val presenterContext: FavoritesPresenterContext
    val favoriteMutationKey: FakeFavoriteTimetableItemIdMutationKey
}
