package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(scope = AppScope::class)
interface DesktopAppGraph : AppGraph
