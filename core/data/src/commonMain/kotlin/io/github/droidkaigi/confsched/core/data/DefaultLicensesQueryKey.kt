package io.github.droidkaigi.confsched.core.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.droidkaigi.confsched.core.model.LicensesJsonProvider
import io.github.droidkaigi.confsched.core.model.LicensesQueryKey
import io.github.droidkaigi.confsched.core.model.SoilIds
import soil.query.buildQueryKey

@Inject
@ContributesBinding(AppScope::class)
class DefaultLicensesQueryKey(
    private val licensesJsonProvider: LicensesJsonProvider,
) : LicensesQueryKey by buildQueryKey(
    id = SoilIds.licensesQuery,
    fetch = { licensesJsonProvider.provide().toLibs() },
)
