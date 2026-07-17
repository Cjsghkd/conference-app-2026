package io.github.droidkaigi.confsched.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.model.KaigiColorScheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull

@Inject
@SingleIn(AppScope::class)
class ThemeStore(@SettingsDataStoreQualifier private val dataStore: DataStore<Preferences>) {

    // First launch assigns a random scheme and persists it before observing — writing from inside
    // the data flow would re-trigger emissions and flicker through random picks.
    fun colorScheme(): Flow<KaigiColorScheme> = flow {
        if (dataStore.data.first().readScheme() == null) {
            saveColorScheme(KaigiColorScheme.entries.random())
        }
        emitAll(dataStore.data.mapNotNull { it.readScheme() })
    }

    suspend fun saveColorScheme(scheme: KaigiColorScheme) {
        dataStore.edit { it[KEY] = scheme.name }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun Preferences.readScheme(): KaigiColorScheme? =
        this[KEY]?.let { name -> KaigiColorScheme.entries.firstOrNull { it.name == name } }

    companion object {
        private val KEY = stringPreferencesKey("theme.colorScheme")
    }
}
