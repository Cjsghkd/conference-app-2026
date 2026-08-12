package io.github.droidkaigi.confsched.feature.staff

import dev.zacsweers.metro.DependencyGraph
import io.github.droidkaigi.confsched.core.model.StaffScreenScope
import io.github.droidkaigi.confsched.core.testing.FakeStaffQueryKey
import io.github.droidkaigi.confsched.core.testing.TestingScope

@DependencyGraph(scope = TestingScope::class, additionalScopes = [StaffScreenScope::class])
interface StaffScreenTestGraph {
    val screenContext: StaffScreenContext
    val presenterContext: StaffPresenterContext
    val staffQueryKey: FakeStaffQueryKey
}
