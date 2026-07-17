package io.github.droidkaigi.confsched.core.preview

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import org.jetbrains.compose.resources.DrawableResource

/**
 * Default [PreviewImageResolver] binding: resolves nothing, so preview sentinel URLs render as
 * blank images. `:core:preview:impl` replaces it with the real resolver on classpaths that
 * include the preview drawables (the IDE preview renderer, the screenshot-test runtime).
 */
@Inject
@ContributesBinding(PreviewScope::class)
class NoopPreviewImageResolver : PreviewImageResolver {
    override fun resolve(imageUrl: String): DrawableResource? = null
}
