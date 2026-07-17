package io.github.droidkaigi.confsched.core.data

import dev.zacsweers.metro.Qualifier

/** Qualifies the settings `DataStore<Preferences>` (theme and other small user preferences). */
@Qualifier
annotation class SettingsDataStoreQualifier

internal const val SETTINGS_DATA_STORE_FILE_NAME = "confsched2026.preferences_pb"
