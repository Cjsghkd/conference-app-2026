package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenContext
import io.github.droidkaigi.confsched.core.model.SoilErrorsScreenScope

@Inject
class SoilErrorsPresenterContext(
    val soilErrorMonitor: DebugSoilErrorMonitor,
) : PresenterContext

@Inject
@SingleIn(SoilErrorsScreenScope::class)
class SoilErrorsScreenContext(
    val presenterContext: SoilErrorsPresenterContext,
) : ScreenContext
