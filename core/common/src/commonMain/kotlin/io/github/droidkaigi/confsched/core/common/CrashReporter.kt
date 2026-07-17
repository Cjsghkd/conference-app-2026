package io.github.droidkaigi.confsched.core.common

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

/**
 * Receives every KaigiLogger.error call. The default reports nowhere; a production crash-reporting
 * module (Firebase Crashlytics via Kermit's CrashlyticsLogWriter or CrashKiOS) replaces
 * [CrashReporterDefaults] once the Firebase project configuration exists.
 */
fun interface CrashReporter {
    fun report(throwable: Throwable?, message: String)
}

@ContributesTo(AppScope::class)
interface CrashReporterDefaults {
    @Provides
    fun provideCrashReporter(): CrashReporter = CrashReporter { _, _ -> }
}
