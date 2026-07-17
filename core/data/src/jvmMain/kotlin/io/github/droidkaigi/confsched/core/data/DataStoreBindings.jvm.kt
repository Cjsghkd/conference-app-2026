package io.github.droidkaigi.confsched.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import okio.Path.Companion.toPath

internal actual fun createDataStore(pathProducer: DataStorePathProducer, fileName: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath { pathProducer.producePath(fileName).toPath() }

@ContributesTo(AppScope::class)
interface DataStorePathProducerBindings {
    @Provides
    fun provideDataStorePathProducer(): DataStorePathProducer = DataStorePathProducer { fileName ->
        "${System.getProperty("user.home")}/.confsched/$fileName"
    }
}
