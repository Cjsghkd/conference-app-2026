package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.data.ContributorListResponse
import io.github.droidkaigi.confsched.core.data.ContributorsApi
import io.github.droidkaigi.confsched.core.data.ContributorsApiProvider
import io.github.droidkaigi.confsched.core.data.DefaultContributorsApiProvider
import io.github.droidkaigi.confsched.core.data.FakeContributorsApi
import io.github.droidkaigi.confsched.core.data.KtorfitFactory
import io.github.droidkaigi.confsched.core.data.ServerEnvironment
import io.github.droidkaigi.confsched.core.data.ServerEnvironmentStore
import io.github.droidkaigi.confsched.core.data.createContributorsApi

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, replaces = [DefaultContributorsApiProvider::class])
class EnvironmentAwareContributorsApiProvider(
    private val ktorfitFactory: KtorfitFactory,
    private val store: ServerEnvironmentStore,
) : ContributorsApiProvider {

    override val api: ContributorsApi = EnvironmentAwareContributorsApi()

    private inner class EnvironmentAwareContributorsApi : ContributorsApi {
        private val fake = FakeContributorsApi()
        private val remotes = mutableMapOf<ServerEnvironment, ContributorsApi>()

        override suspend fun getContributors(): ContributorListResponse = current().getContributors()

        private fun current(): ContributorsApi {
            val environment = store.environment.value
            val baseUrl = environment.baseUrl ?: return fake
            return remotes.getOrPut(environment) { ktorfitFactory.create(baseUrl).createContributorsApi() }
        }
    }
}
