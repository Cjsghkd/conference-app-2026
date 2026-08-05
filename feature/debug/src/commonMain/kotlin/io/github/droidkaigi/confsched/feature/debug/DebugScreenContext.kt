package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.KaigiClock
import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenContext
import io.github.droidkaigi.confsched.core.data.PersistedDataResetter
import io.github.droidkaigi.confsched.core.model.DebugScreenScope
import io.github.droidkaigi.confsched.core.model.SoilErrorOverlayEnabledMutationKey
import io.github.droidkaigi.confsched.core.model.buildconfig.BuildConfigProvider

@Inject
class DebugPresenterContext(
    val buildConfig: BuildConfigProvider,
    val persistedDataResetter: PersistedDataResetter,
    val debugPreferencesStore: DebugPreferencesStore,
    val soilErrorMonitor: DebugSoilErrorMonitor,
    val soilErrorOverlayEnabledMutationKey: SoilErrorOverlayEnabledMutationKey,
    val clock: KaigiClock,
    val clockOffsetStore: KaigiClockOffsetStore,
) : PresenterContext

@Inject
@SingleIn(DebugScreenScope::class)
class DebugScreenContext(
    val presenterContext: DebugPresenterContext,
) : ScreenContext
