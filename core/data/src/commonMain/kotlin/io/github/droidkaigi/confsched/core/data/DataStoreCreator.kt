package io.github.droidkaigi.confsched.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** Produces the absolute path for a preferences file; the only per-platform piece of DataStore setup. */
fun interface DataStorePathProducer {
    fun producePath(fileName: String): String
}

// wasmJs has no filesystem: its actual ignores the path producer and persists to localStorage.
internal expect fun createDataStore(pathProducer: DataStorePathProducer, fileName: String): DataStore<Preferences>
