package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.droidkaigi.confsched.core.data.ServerEnvironmentSelectionMutationKey
import io.github.droidkaigi.confsched.core.data.ServerEnvironmentStore
import io.github.droidkaigi.confsched.core.data.SoilIds
import io.github.droidkaigi.confsched.core.model.MutationTag
import io.github.droidkaigi.confsched.core.model.ServerEnvironmentScreenScope
import soil.query.buildMutationKey

@Inject
@ContributesBinding(ServerEnvironmentScreenScope::class)
class DefaultServerEnvironmentSelectionMutationKey(
    extraTag: MutationTag,
    private val debugPreferencesStore: DebugPreferencesStore,
    private val serverEnvironmentStore: ServerEnvironmentStore,
) : ServerEnvironmentSelectionMutationKey by buildMutationKey(
    id = SoilIds.serverEnvironmentSelectionMutation(extraTag),
    mutate = { selection ->
        debugPreferencesStore.setServerEnvironmentSelection(
            environment = selection.environment,
            skipSelectionNextLaunch = selection.skipSelectionNextLaunch,
        )
        serverEnvironmentStore.select(selection.environment)
    },
)
