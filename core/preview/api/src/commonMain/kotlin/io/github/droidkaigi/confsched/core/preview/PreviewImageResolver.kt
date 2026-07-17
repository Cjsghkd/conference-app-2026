package io.github.droidkaigi.confsched.core.preview

import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.DrawableResource

fun interface PreviewImageResolver {
    fun resolve(imageUrl: String): DrawableResource?
}

val LocalPreviewImageResolver = staticCompositionLocalOf<PreviewImageResolver?> { null }
