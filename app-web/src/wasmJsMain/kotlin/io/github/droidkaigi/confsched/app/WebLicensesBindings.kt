package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.droidkaigi.confsched.app.web.generated.resources.Res
import io.github.droidkaigi.confsched.core.model.LicensesJsonProvider

@ContributesTo(AppScope::class)
interface WebLicensesBindings {
    @Provides
    fun provideLicensesJsonProvider(): LicensesJsonProvider = LicensesJsonProvider {
        listOf(Res.readBytes("files/aboutlibraries.json").decodeToString())
    }
}
