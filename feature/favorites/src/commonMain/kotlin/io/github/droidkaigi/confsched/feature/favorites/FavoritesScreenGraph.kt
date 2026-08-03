package io.github.droidkaigi.confsched.feature.favorites

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import io.github.droidkaigi.confsched.core.common.UiScope
import io.github.droidkaigi.confsched.core.model.FavoritesScreenScope
import io.github.droidkaigi.confsched.core.model.MutationTag

@GraphExtension(FavoritesScreenScope::class)
interface FavoritesScreenGraph {
    val screenContext: FavoritesScreenContext
    val screenNavigator: FavoritesScreenNavigator

    @Provides
    private fun provideMutationTag(): MutationTag = MutationTag("FavoritesScreen")

    @GraphExtension.Factory
    @ContributesTo(UiScope::class)
    fun interface Factory {
        fun createFavoritesScreenGraph(): FavoritesScreenGraph
    }
}
