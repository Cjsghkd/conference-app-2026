package io.github.droidkaigi.confsched.app

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.droidkaigi.confsched.core.common.CrashReporter
import io.github.droidkaigi.confsched.core.common.CrashReporterDefaults

@ContributesTo(AppScope::class, replaces = [CrashReporterDefaults::class])
interface AndroidCrashReporterBindings {
    @Provides
    fun provideCrashReporter(context: Context): CrashReporter {
        // Without the Firebase project configuration the default initializer registers no
        // FirebaseApp; stay no-op instead of crashing on getInstance().
        if (FirebaseApp.getApps(context).isEmpty()) {
            return CrashReporter { _, _ -> }
        }
        return CrashReporter { throwable, message ->
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log(message)
            crashlytics.recordException(throwable ?: RuntimeException(message))
        }
    }
}
