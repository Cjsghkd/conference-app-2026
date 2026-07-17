package io.github.droidkaigi.confsched.feature.favorites

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenContext
import io.github.droidkaigi.confsched.core.model.FavoritesScreenScope

@Inject
class FavoritesPresenterContext : PresenterContext

@Inject
@SingleIn(FavoritesScreenScope::class)
class FavoritesScreenContext(
    val presenterContext: FavoritesPresenterContext,
) : ScreenContext
