package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.data.DefaultSponsorApiProvider
import io.github.droidkaigi.confsched.core.data.FakeSponsorApi
import io.github.droidkaigi.confsched.core.data.KtorfitFactory
import io.github.droidkaigi.confsched.core.data.ServerEnvironment
import io.github.droidkaigi.confsched.core.data.ServerEnvironmentStore
import io.github.droidkaigi.confsched.core.data.SponsorApi
import io.github.droidkaigi.confsched.core.data.SponsorApiProvider
import io.github.droidkaigi.confsched.core.data.SponsorListResponse
import io.github.droidkaigi.confsched.core.data.createSponsorApi

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, replaces = [DefaultSponsorApiProvider::class])
class EnvironmentAwareSponsorApiProvider(
    private val ktorfitFactory: KtorfitFactory,
    private val store: ServerEnvironmentStore,
) : SponsorApiProvider {

    override val api: SponsorApi = EnvironmentAwareSponsorApi()

    private inner class EnvironmentAwareSponsorApi : SponsorApi {
        private val fake = FakeSponsorApi()
        private val remotes = mutableMapOf<ServerEnvironment, SponsorApi>()

        override suspend fun getSponsors(): SponsorListResponse = current().getSponsors()

        private fun current(): SponsorApi {
            val environment = store.environment.value
            val baseUrl = environment.baseUrl ?: return fake
            return remotes.getOrPut(environment) { ktorfitFactory.create(baseUrl).createSponsorApi() }
        }
    }
}
