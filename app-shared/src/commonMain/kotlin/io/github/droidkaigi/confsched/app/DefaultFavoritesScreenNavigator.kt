package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.AppNavigator
import io.github.droidkaigi.confsched.core.model.FavoritesScreenScope
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.feature.favorites.FavoritesScreenNavigator
import io.github.droidkaigi.confsched.feature.sessions.timetable.TimetableItemDetailNavKey

@Inject
@SingleIn(FavoritesScreenScope::class)
@ContributesBinding(FavoritesScreenScope::class)
class DefaultFavoritesScreenNavigator(
    private val appNavigator: AppNavigator,
) : FavoritesScreenNavigator {
    override fun openSessionDetail(id: TimetableItemId) {
        appNavigator.goTo(TimetableItemDetailNavKey(id))
    }
}
