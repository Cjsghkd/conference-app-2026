package io.github.droidkaigi.confsched.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedValuesStoreRegistry
import androidx.compose.runtime.retain.retainRetainedValuesStoreRegistry
import androidx.navigation3.runtime.NavEntryDecorator

@Composable
fun <T : Any> retainNavEntryDecorator(): NavEntryDecorator<T> {
    val registry: RetainedValuesStoreRegistry = retainRetainedValuesStoreRegistry()
    return remember(registry) {
        NavEntryDecorator(onPop = registry::clearChild) { entry ->
            registry.LocalRetainedValuesStoreProvider(entry.contentKey) {
                entry.Content()
            }
        }
    }
}
