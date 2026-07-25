package io.github.droidkaigi.confsched.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.zacsweers.metro.createGraph
import io.github.droidkaigi.confsched.core.common.context

fun main() {
    val graph = createGraph<DesktopAppGraph>()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "DroidKaigi 2026 — Timetable",
        ) {
            context(graph) {
                KaigiApp()
            }
        }
    }
}
