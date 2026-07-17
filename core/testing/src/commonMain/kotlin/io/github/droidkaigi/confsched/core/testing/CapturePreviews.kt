package io.github.droidkaigi.confsched.core.testing

import io.github.droidkaigi.confsched.core.common.RegisteredPreview

/**
 * Renders every registry entry and runs it through the Roborazzi pipeline. The desktop and iOS
 * actuals capture into their platform screenshot directory; Android (covered by the
 * Roborazzi-generated Robolectric preview tests) and wasmJs (no Roborazzi artifact) are no-ops.
 */
expect fun capturePreviews(previews: List<RegisteredPreview>)
