package io.github.droidkaigi.confsched.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

// The browser has no filesystem: localStorage entries are keyed by file name, and the path
// producer is never consulted.
internal actual fun createDataStore(pathProducer: DataStorePathProducer, fileName: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
        storage = WebLocalStorage(name = fileName, serializer = PreferencesSerializer),
    )

@ContributesTo(AppScope::class)
interface DataStorePathProducerBindings {
    @Provides
    fun provideDataStorePathProducer(): DataStorePathProducer = DataStorePathProducer { it }
}
