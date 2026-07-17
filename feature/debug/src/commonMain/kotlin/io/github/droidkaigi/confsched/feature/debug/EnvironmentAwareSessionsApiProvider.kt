package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.data.DefaultSessionsApiProvider
import io.github.droidkaigi.confsched.core.data.FakeSessionsApi
import io.github.droidkaigi.confsched.core.data.KtorfitFactory
import io.github.droidkaigi.confsched.core.data.ServerEnvironment
import io.github.droidkaigi.confsched.core.data.ServerEnvironmentStore
import io.github.droidkaigi.confsched.core.data.SessionsAllResponse
import io.github.droidkaigi.confsched.core.data.SessionsApi
import io.github.droidkaigi.confsched.core.data.SessionsApiProvider
import io.github.droidkaigi.confsched.core.data.createSessionsApi

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, replaces = [DefaultSessionsApiProvider::class])
class EnvironmentAwareSessionsApiProvider(
    private val ktorfitFactory: KtorfitFactory,
    private val store: ServerEnvironmentStore,
) : SessionsApiProvider {

    override val api: SessionsApi = EnvironmentAwareSessionsApi()

    private inner class EnvironmentAwareSessionsApi : SessionsApi {
        private val fake = FakeSessionsApi()
        private val remotes = mutableMapOf<ServerEnvironment, SessionsApi>()

        override suspend fun getTimetable(): SessionsAllResponse = current().getTimetable()

        private fun current(): SessionsApi {
            val environment = store.environment.value
            val baseUrl = environment.baseUrl ?: return fake
            return remotes.getOrPut(environment) { ktorfitFactory.create(baseUrl).createSessionsApi() }
        }
    }
}
