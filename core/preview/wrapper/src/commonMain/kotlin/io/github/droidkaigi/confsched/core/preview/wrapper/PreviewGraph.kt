package io.github.droidkaigi.confsched.core.preview.wrapper

import dev.zacsweers.metro.DependencyGraph
import io.github.droidkaigi.confsched.core.preview.PreviewImageResolver
import io.github.droidkaigi.confsched.core.preview.PreviewScope

@DependencyGraph(scope = PreviewScope::class)
interface PreviewGraph {
    val previewImageResolver: PreviewImageResolver
}
