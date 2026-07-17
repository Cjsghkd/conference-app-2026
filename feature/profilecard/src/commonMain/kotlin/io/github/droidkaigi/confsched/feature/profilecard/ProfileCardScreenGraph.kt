package io.github.droidkaigi.confsched.feature.profilecard

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.droidkaigi.confsched.core.model.ProfileCardScreenScope

@GraphExtension(ProfileCardScreenScope::class)
interface ProfileCardScreenGraph {
    val screenContext: ProfileCardScreenContext
    val screenNavigator: ProfileCardScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(AppScope::class)
    fun interface Factory {
        fun createProfileCardScreenGraph(): ProfileCardScreenGraph
    }
}
