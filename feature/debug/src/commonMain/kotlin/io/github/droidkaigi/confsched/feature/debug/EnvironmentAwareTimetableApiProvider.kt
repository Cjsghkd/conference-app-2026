package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.data.DefaultTimetableApiProvider
import io.github.droidkaigi.confsched.core.data.FakeTimetableApi
import io.github.droidkaigi.confsched.core.data.KtorfitFactory
import io.github.droidkaigi.confsched.core.data.ServerEnvironment
import io.github.droidkaigi.confsched.core.data.ServerEnvironmentStore
import io.github.droidkaigi.confsched.core.data.TimetableResponse
import io.github.droidkaigi.confsched.core.data.TimetableApi
import io.github.droidkaigi.confsched.core.data.TimetableApiProvider
import io.github.droidkaigi.confsched.core.data.createTimetableApi

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, replaces = [DefaultTimetableApiProvider::class])
class EnvironmentAwareTimetableApiProvider(
    private val ktorfitFactory: KtorfitFactory,
    private val store: ServerEnvironmentStore,
) : TimetableApiProvider {

    override val api: TimetableApi = EnvironmentAwareTimetableApi()

    private inner class EnvironmentAwareTimetableApi : TimetableApi {
        private val fake = FakeTimetableApi()
        private val remotes = mutableMapOf<ServerEnvironment, TimetableApi>()

        override suspend fun getTimetable(): TimetableResponse = current().getTimetable()

        private fun current(): TimetableApi {
            val environment = store.environment.value
            val baseUrl = environment.baseUrl ?: return fake
            return remotes.getOrPut(environment) { ktorfitFactory.create(baseUrl).createTimetableApi() }
        }
    }
}
