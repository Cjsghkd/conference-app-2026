package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.AppScope

@DependencyGraph(scope = AppScope::class)
interface DesktopAppGraph : AppGraph
