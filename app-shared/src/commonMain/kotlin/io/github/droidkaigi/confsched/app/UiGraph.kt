package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.droidkaigi.confsched.core.common.AppNavigator
import io.github.droidkaigi.confsched.core.common.UiScope

@GraphExtension(UiScope::class)
interface UiGraph {
    val appNavigator: AppNavigator
    val appEntryProvider: AppEntryProvider

    @GraphExtension.Factory
    @ContributesTo(AppScope::class)
    fun interface Factory {
        fun createUiGraph(): UiGraph
    }
}
