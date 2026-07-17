package io.github.droidkaigi.confsched.core.testing

import com.github.takahirom.roborazzi.ComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester.TestParameter.JUnit4TestParameter
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.common.CommonPreviewInfo
import sergio.sastre.composable.preview.scanner.common.screenshotid.CommonPreviewScreenshotIdBuilder

/**
 * ComposePreviewTester used by the tests that the Roborazzi Gradle plugin generates
 * (generateComposePreviewRobolectricTests). The default tester scans the androidx `@Preview`
 * annotation, which this project does not use; this one scans Compose Multiplatform's
 * `org.jetbrains.compose.ui.tooling.preview.Preview` via CommonComposablePreviewScanner.
 */
@OptIn(ExperimentalRoborazziApi::class)
class KaigiComposePreviewTester : ComposePreviewTester<JUnit4TestParameter<CommonPreviewInfo>> {

    override fun testParameters(): List<JUnit4TestParameter<CommonPreviewInfo>> {
        val options = options()
        val lifecycleOptions =
            options.testLifecycleOptions as ComposePreviewTester.Options.JUnit4TestLifecycleOptions
        return CommonComposablePreviewScanner()
            .scanPackageTrees(*options.scanOptions.packages.toTypedArray())
            .getPreviews()
            .map { preview ->
                JUnit4TestParameter(
                    composeTestRuleFactory = lifecycleOptions.composeRuleFactory,
                    preview = preview,
                )
            }
    }

    override fun test(testParameter: JUnit4TestParameter<CommonPreviewInfo>) {
        val preview = testParameter.preview
        val id = CommonPreviewScreenshotIdBuilder(preview).build()
        captureRoboImage(filePath = "screenshots/$id.png") {
            preview()
        }
    }
}
