package io.github.droidkaigi.confsched.core.testing

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.DefaultFileNameGenerator
import com.github.takahirom.roborazzi.InternalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage

@OptIn(ExperimentalTestApi::class, InternalRoborazziApi::class)
internal actual fun ComposeUiTest.captureRobotScreen(name: String) {
    // Roborazzi resolves a bare name against the working directory, so the generator supplies the
    // configured output directory and the test class; only the scenario name is substituted in.
    val directory = DefaultFileNameGenerator.generateFilePath()
        .substringBeforeLast('/', missingDelimiterValue = "")
    val prefix = if (directory.isEmpty()) "" else "$directory/"
    // Capturing a node defaults to dumping the semantics tree; the report wants the rendered screen.
    onRoot().captureRoboImage(
        filePath = "$prefix$name.png",
        roborazziOptions = RoborazziOptions(captureType = RoborazziOptions.CaptureType.Screenshot()),
    )
}
