package io.github.droidkaigi.confsched.feature.favorites

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.droidkaigi.confsched.core.model.FavoritesScreenScope

@GraphExtension(FavoritesScreenScope::class)
interface FavoritesScreenGraph {
    val screenContext: FavoritesScreenContext
    val screenNavigator: FavoritesScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(AppScope::class)
    fun interface Factory {
        fun createFavoritesScreenGraph(): FavoritesScreenGraph
    }
}
