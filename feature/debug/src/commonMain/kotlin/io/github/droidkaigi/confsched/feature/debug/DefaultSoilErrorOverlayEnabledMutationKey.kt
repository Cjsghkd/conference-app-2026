package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.droidkaigi.confsched.core.model.DebugScreenScope
import io.github.droidkaigi.confsched.core.model.MutationTag
import soil.query.buildMutationKey

@Inject
@SoilErrorOverlayEnabled
@ContributesBinding(DebugScreenScope::class)
class DefaultSoilErrorOverlayEnabledMutationKey(
    extraTag: MutationTag,
    private val debugPreferencesStore: DebugPreferencesStore,
) : SoilErrorOverlayEnabledMutationKey by buildMutationKey(
    id = SoilIds.soilErrorOverlayEnabledMutation(extraTag),
    mutate = { enabled -> debugPreferencesStore.setSoilErrorOverlayEnabled(enabled) },
)
