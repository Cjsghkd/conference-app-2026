package io.github.droidkaigi.confsched.core.data

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.AppScope
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Inject
@SingleIn(AppScope::class)
class FavoritesStore {
    private val state = MutableStateFlow<PersistentSet<TimetableItemId>>(persistentSetOf())

    fun favoriteIds(): Flow<PersistentSet<TimetableItemId>> = state.asStateFlow()

    fun toggle(id: TimetableItemId) {
        state.update { current ->
            if (id in current) current.removing(id) else current.adding(id)
        }
    }

    fun clear() {
        state.value = persistentSetOf()
    }
}
