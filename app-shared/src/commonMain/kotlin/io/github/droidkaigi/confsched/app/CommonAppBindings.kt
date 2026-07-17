package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import soil.query.SwrCachePlus
import soil.query.SwrCachePlusPolicy
import soil.query.SwrCacheScope
import soil.query.SwrClientPlus
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.core.ErrorRelay

@ContributesTo(AppScope::class)
interface CommonAppBindings {
    // With no receiver the relay retains only the latest record (DROP_OLDEST), so an always-on binding is safe in production.
    @Provides
    @SingleIn(AppScope::class)
    fun provideErrorRelay(): ErrorRelay = ErrorRelay.newAnycast(CoroutineScope(SupervisorJob()))

    @OptIn(ExperimentalSoilQueryApi::class)
    @Provides
    @SingleIn(AppScope::class)
    fun provideSwrClient(errorRelay: ErrorRelay): SwrClientPlus = SwrCachePlus(
        SwrCachePlusPolicy(
            coroutineScope = SwrCacheScope(),
            errorRelay = errorRelay,
        ),
    )
}
