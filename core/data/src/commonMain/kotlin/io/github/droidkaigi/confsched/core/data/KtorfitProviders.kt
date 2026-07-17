package io.github.droidkaigi.confsched.core.data

import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

fun interface KtorfitFactory {
    fun create(baseUrl: String): Ktorfit
}

@ContributesTo(AppScope::class)
interface KtorfitProviders {
    @Provides
    @SingleIn(AppScope::class)
    fun provideKtorfitFactory(httpClient: HttpClient): KtorfitFactory =
        KtorfitFactory { baseUrl -> Ktorfit.Builder().baseUrl(baseUrl).httpClient(httpClient).build() }

    // Production Ktorfit is the unqualified default; per-environment instances are built via KtorfitFactory.
    @Provides
    @SingleIn(AppScope::class)
    fun provideKtorfit(factory: KtorfitFactory): Ktorfit =
        factory.create(requireNotNull(ServerEnvironment.Production.baseUrl))
}
