package io.github.droidkaigi.confsched.feature.about

import dev.zacsweers.metro.DependencyGraph
import io.github.droidkaigi.confsched.core.model.LicensesScreenScope
import io.github.droidkaigi.confsched.core.testing.FakeLicensesQueryKey
import io.github.droidkaigi.confsched.core.testing.TestingScope

@DependencyGraph(scope = TestingScope::class, additionalScopes = [LicensesScreenScope::class])
interface LicensesScreenTestGraph {
    val screenContext: LicensesScreenContext
    val presenterContext: LicensesPresenterContext
    val licensesQueryKey: FakeLicensesQueryKey
}
