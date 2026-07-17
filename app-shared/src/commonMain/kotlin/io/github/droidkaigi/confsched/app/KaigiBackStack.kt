package io.github.droidkaigi.confsched.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.droidkaigi.confsched.feature.sessions.timetable.TimetableNavKey

// Created by the platform shell and passed into KaigiApp, so shells (the iOS view controller)
// can observe the back stack.
context(appGraph: AppGraph)
@Composable
fun rememberKaigiBackStack(): NavBackStack<NavKey> = rememberNavBackStack(
    configuration = remember(appGraph.navKeySerializersProvider) {
        SavedStateConfiguration {
            serializersModule = appGraph.navKeySerializersProvider.serializersModule
        }
    },
    remember { appGraph.initialNavKeyOverrideProvider.initialNavKeyOverride ?: TimetableNavKey },
)
