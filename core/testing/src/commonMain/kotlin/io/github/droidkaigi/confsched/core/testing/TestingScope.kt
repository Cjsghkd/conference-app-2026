package io.github.droidkaigi.confsched.core.testing

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.droidkaigi.confsched.core.model.MutationTag

sealed interface TestingScope

@ContributesTo(TestingScope::class)
interface TestingBindings {
    // Each test graph owns its SwrClient, so one tag for all of them keeps the cache isolated.
    @Provides
    fun provideMutationTag(): MutationTag = MutationTag("Testing")
}
