package io.github.droidkaigi.confsched.core.common

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.modules.SerializersModule

interface NavKeySerializersProvider {
    val serializersModule: SerializersModule
}

@Inject
@SingleIn(AppScope::class)
class MergedNavKeySerializersProvider(providers: Set<NavKeySerializersProvider>) : NavKeySerializersProvider {
    override val serializersModule: SerializersModule = SerializersModule {
        providers.forEach { provider ->
            include(provider.serializersModule)
        }
    }
}
