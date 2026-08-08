package io.github.droidkaigi.confsched.feature.sponsors

import dev.zacsweers.metro.DependencyGraph
import io.github.droidkaigi.confsched.core.model.SponsorsScreenScope
import io.github.droidkaigi.confsched.core.testing.FakeSponsorsQueryKey
import io.github.droidkaigi.confsched.core.testing.TestingScope

@DependencyGraph(scope = TestingScope::class, additionalScopes = [SponsorsScreenScope::class])
interface SponsorsScreenTestGraph {
    val screenContext: SponsorsScreenContext
    val presenterContext: SponsorsPresenterContext
    val sponsorsQueryKey: FakeSponsorsQueryKey
}
