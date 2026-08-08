package io.github.droidkaigi.confsched.app

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.droidkaigi.confsched.R
import io.github.droidkaigi.confsched.core.model.LicensesJsonProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ContributesTo(AppScope::class)
interface AndroidLicensesBindings {
    @Provides
    fun provideLicensesJsonProvider(context: Context): LicensesJsonProvider = LicensesJsonProvider {
        withContext(Dispatchers.IO) {
            listOf(context.resources.openRawResource(R.raw.aboutlibraries).use { it.readBytes().decodeToString() })
        }
    }
}
