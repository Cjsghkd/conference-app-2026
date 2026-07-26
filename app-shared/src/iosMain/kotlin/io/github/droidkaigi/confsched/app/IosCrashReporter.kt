@file:OptIn(ExperimentalForeignApi::class)

package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.droidkaigi.confsched.core.common.CrashReporter
import io.github.droidkaigi.confsched.core.common.CrashReporterDefaults
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import swiftPMImport.conference.app.`2026`.app.shared.FIRApp
import swiftPMImport.conference.app.`2026`.app.shared.FIRCrashlytics

@ContributesTo(AppScope::class, replaces = [CrashReporterDefaults::class])
interface IosCrashReporterBindings {
    @Provides
    fun provideCrashReporter(): CrashReporter {
        // Crashlytics needs the Firebase project configuration; without the plist stay no-op.
        if (NSBundle.mainBundle.pathForResource("GoogleService-Info", ofType = "plist") == null) {
            return CrashReporter { _, _ -> }
        }
        if (FIRApp.defaultApp() == null) {
            FIRApp.configure()
        }
        return CrashReporter { throwable, message ->
            val crashlytics = FIRCrashlytics.crashlytics()
            throwable?.let { crashlytics.log(it.toString()) }
            crashlytics.recordError(
                NSError.errorWithDomain(
                    domain = "io.github.droidkaigi.confsched",
                    code = 0,
                    userInfo = mapOf<Any?, Any?>(NSLocalizedDescriptionKey to message),
                ),
            )
        }
    }
}
