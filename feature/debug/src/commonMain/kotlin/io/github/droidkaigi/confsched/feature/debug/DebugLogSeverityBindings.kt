package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.droidkaigi.confsched.core.common.MinLogSeverity
import io.github.droidkaigi.confsched.core.common.MinLogSeverityDefaults

@ContributesTo(AppScope::class, replaces = [MinLogSeverityDefaults::class])
interface DebugLogSeverityBindings {
    @Provides
    fun provideMinLogSeverity(): MinLogSeverity = MinLogSeverity.Verbose
}
