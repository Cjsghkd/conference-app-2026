package io.github.droidkaigi.confsched.core.preview.wrapper

import dev.zacsweers.metro.createGraph
import io.github.droidkaigi.confsched.core.preview.PreviewImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PreviewWiringTest {
    @Test
    fun previewResolverIsWiredAndResolves() {
        val resolver = createGraph<PreviewGraph>().previewImageResolver
        assertEquals("DefaultPreviewImageResolver", resolver::class.simpleName)
        assertNotNull(resolver.resolve(PreviewImage.SessionCover.imageUrl))
        assertNull(resolver.resolve("https://example.com/not-a-preview.png"))
    }
}
