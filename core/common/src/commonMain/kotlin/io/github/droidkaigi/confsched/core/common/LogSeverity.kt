package io.github.droidkaigi.confsched.core.common

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

enum class MinLogSeverity {
    Verbose,
    ErrorOnly,
}

/** Production default: debug/info/warn are muted. The debug build replaces this with [MinLogSeverity.Verbose]. */
@ContributesTo(AppScope::class)
interface MinLogSeverityDefaults {
    @Provides
    fun provideMinLogSeverity(): MinLogSeverity = MinLogSeverity.ErrorOnly
}
