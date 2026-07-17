package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.AppNavigator
import io.github.droidkaigi.confsched.core.model.FavoritesScreenScope
import io.github.droidkaigi.confsched.feature.favorites.FavoritesScreenNavigator

@Inject
@SingleIn(FavoritesScreenScope::class)
@ContributesBinding(FavoritesScreenScope::class)
class DefaultFavoritesScreenNavigator(
    @Suppress("unused") private val appNavigator: AppNavigator,
) : FavoritesScreenNavigator
