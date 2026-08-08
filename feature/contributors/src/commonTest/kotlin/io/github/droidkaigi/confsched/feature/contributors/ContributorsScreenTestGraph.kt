package io.github.droidkaigi.confsched.feature.contributors

import dev.zacsweers.metro.DependencyGraph
import io.github.droidkaigi.confsched.core.model.ContributorsScreenScope
import io.github.droidkaigi.confsched.core.testing.FakeContributorsQueryKey
import io.github.droidkaigi.confsched.core.testing.TestingScope

@DependencyGraph(scope = TestingScope::class, additionalScopes = [ContributorsScreenScope::class])
interface ContributorsScreenTestGraph {
    val screenContext: ContributorsScreenContext
    val presenterContext: ContributorsPresenterContext
    val contributorsQueryKey: FakeContributorsQueryKey
}
