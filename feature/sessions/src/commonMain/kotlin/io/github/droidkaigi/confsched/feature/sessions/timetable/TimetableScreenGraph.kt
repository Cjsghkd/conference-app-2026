package io.github.droidkaigi.confsched.feature.sessions.timetable

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import io.github.droidkaigi.confsched.core.model.MutationTag
import io.github.droidkaigi.confsched.core.model.TimetableScreenScope

@GraphExtension(TimetableScreenScope::class)
interface TimetableScreenGraph {
    val screenContext: TimetableScreenContext

    val screenNavigator: TimetableScreenNavigator

    @Provides
    private fun provideMutationTag(): MutationTag = MutationTag("TimetableScreen")

    @GraphExtension.Factory
    @ContributesTo(AppScope::class)
    fun interface Factory {
        fun createTimetableScreenGraph(): TimetableScreenGraph
    }
}
