package io.github.droidkaigi.confsched.feature.debug

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.data.ServerEnvironment
import io.github.droidkaigi.confsched.core.data.SettingsDataStoreQualifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
@SingleIn(AppScope::class)
class DebugPreferencesStore(
    @SettingsDataStoreQualifier private val dataStore: DataStore<Preferences>,
) {
    val soilErrorOverlayEnabled: Flow<Boolean> =
        dataStore.data.map { it[SOIL_ERROR_OVERLAY_ENABLED_KEY] ?: true }

    suspend fun setSoilErrorOverlayEnabled(enabled: Boolean) {
        dataStore.edit { it[SOIL_ERROR_OVERLAY_ENABLED_KEY] = enabled }
    }

    val serverEnvironment: Flow<ServerEnvironment?> = dataStore.data.map { preferences ->
        preferences[SERVER_ENVIRONMENT_KEY]?.let { name ->
            ServerEnvironment.entries.firstOrNull { it.name == name }
        }
    }

    val skipServerSelection: Flow<Boolean> =
        dataStore.data.map { it[SKIP_SERVER_SELECTION_KEY] ?: false }

    suspend fun setServerEnvironmentSelection(environment: ServerEnvironment, skipSelectionNextLaunch: Boolean) {
        dataStore.edit {
            it[SERVER_ENVIRONMENT_KEY] = environment.name
            it[SKIP_SERVER_SELECTION_KEY] = skipSelectionNextLaunch
        }
    }

    companion object {
        private val SOIL_ERROR_OVERLAY_ENABLED_KEY = booleanPreferencesKey("debug.soilErrorOverlayEnabled")
        private val SERVER_ENVIRONMENT_KEY = stringPreferencesKey("debug.serverEnvironment")
        private val SKIP_SERVER_SELECTION_KEY = booleanPreferencesKey("debug.skipServerSelection")
    }
}
