package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.droidkaigi.confsched.core.model.DebugScreenScope
import io.github.droidkaigi.confsched.core.model.MutationTag
import soil.query.buildMutationKey

@Inject
@ClockOverlayEnabled
@ContributesBinding(DebugScreenScope::class)
class DefaultClockOverlayEnabledMutationKey(
    extraTag: MutationTag,
    private val debugPreferencesStore: DebugPreferencesStore,
) : ClockOverlayEnabledMutationKey by buildMutationKey(
    id = SoilIds.clockOverlayEnabledMutation(extraTag),
    mutate = { enabled -> debugPreferencesStore.setClockOverlayEnabled(enabled) },
)
