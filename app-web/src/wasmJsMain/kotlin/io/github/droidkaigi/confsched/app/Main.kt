package io.github.droidkaigi.confsched.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.zacsweers.metro.createGraph
import io.github.droidkaigi.confsched.core.common.context
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val graph = createGraph<WebAppGraph>()
    ComposeViewport(document.body!!) {
        context(graph) {
            KaigiApp(backStack = rememberKaigiBackStack())
        }
    }
}
