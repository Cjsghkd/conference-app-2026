package io.github.droidkaigi.confsched.core.preview.wrapper

import dev.zacsweers.metro.createGraph
import io.github.droidkaigi.confsched.core.preview.PreviewImageResolver

class PreviewGraphImageResolver : PreviewImageResolver {
    private val delegate = createGraph<PreviewGraph>().previewImageResolver

    override fun resolve(imageUrl: String) = delegate.resolve(imageUrl)
}
