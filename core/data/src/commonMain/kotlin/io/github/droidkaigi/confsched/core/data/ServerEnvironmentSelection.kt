package io.github.droidkaigi.confsched.core.data

import soil.query.MutationKey

data class ServerEnvironmentSelection(
    val environment: ServerEnvironment,
    val skipSelectionNextLaunch: Boolean,
)

typealias ServerEnvironmentSelectionMutationKey = MutationKey<Unit, ServerEnvironmentSelection>
