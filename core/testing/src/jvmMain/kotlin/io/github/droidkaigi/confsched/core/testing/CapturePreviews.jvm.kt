package io.github.droidkaigi.confsched.core.testing

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import io.github.droidkaigi.confsched.core.common.RegisteredPreview
import io.github.takahirom.roborazzi.captureRoboImage

@OptIn(ExperimentalTestApi::class)
actual fun capturePreviews(previews: List<RegisteredPreview>) {
    for (preview in previews) {
        runComposeUiTest {
            setContent { preview.content() }
            onRoot().captureRoboImage(filePath = "screenshots/desktop/${preview.name}.png")
        }
    }
}
