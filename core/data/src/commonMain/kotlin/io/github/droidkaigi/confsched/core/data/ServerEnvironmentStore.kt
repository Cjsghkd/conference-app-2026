package io.github.droidkaigi.confsched.core.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
class ServerEnvironmentStore {
    private val mutableEnvironment = MutableStateFlow(ServerEnvironment.Staging)
    val environment: StateFlow<ServerEnvironment> = mutableEnvironment.asStateFlow()

    fun select(environment: ServerEnvironment) {
        mutableEnvironment.value = environment
    }
}
