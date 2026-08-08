package io.github.droidkaigi.confsched.feature.about

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenContext
import io.github.droidkaigi.confsched.core.model.LicensesQueryKey
import io.github.droidkaigi.confsched.core.model.LicensesScreenScope

@Inject
class LicensesPresenterContext : PresenterContext

@Inject
@SingleIn(LicensesScreenScope::class)
class LicensesScreenContext(
    val licensesQueryKey: LicensesQueryKey,
    val presenterContext: LicensesPresenterContext,
) : ScreenContext
