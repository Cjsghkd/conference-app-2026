package io.github.droidkaigi.confsched.feature.staff

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenContext
import io.github.droidkaigi.confsched.core.model.StaffQueryKey
import io.github.droidkaigi.confsched.core.model.StaffScreenScope

@Inject
class StaffPresenterContext : PresenterContext

@Inject
@SingleIn(StaffScreenScope::class)
class StaffScreenContext(
    val staffQueryKey: StaffQueryKey,
    val presenterContext: StaffPresenterContext,
) : ScreenContext
