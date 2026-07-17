package io.github.droidkaigi.confsched.core.common

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

interface NavEntryProvider {
    fun EntryProviderScope<NavKey>.register()
}
