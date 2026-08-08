package droidkaigi.primitive

import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("io.github.takahirom.roborazzi")
}

val libs = the<LibrariesForLibs>()

// The scan scope always matches the module package (derived from the project path, the same rule
// as the android namespace).
val screenshotPackage = "io.github.droidkaigi.confsched" + project.path.replace(":", ".")

configure<RoborazziExtension> {
    generateComposePreviewRobolectricTests {
        enable.set(true)
        packages.set(listOf(screenshotPackage))
        robolectricConfig.set(
            mapOf(
                "sdk" to "[34]",
                "qualifiers" to "\"w360dp-h800dp-xhdpi\"",
            ),
        )
    }
}

// Non-JVM targets cannot scan the classpath for previews, so a KSP-generated PreviewRegistry
// (tools:ksp-processor) enumerates them; the desktop and iOS screenshot tests below capture
// every registry entry through Roborazzi's desktop / iOS artifacts.
pluginManager.withPlugin("com.google.devtools.ksp") {
    configure<com.google.devtools.ksp.gradle.KspExtension> {
        arg("droidkaigi.previewRegistryPackage", screenshotPackage)
    }
}

// One class in commonTest is enough: capturePreviews is expect/actual in :core:testing (desktop
// and iOS capture; Android and wasmJs actuals are no-ops).
val generatePreviewScreenshotTest = tasks.register("generatePreviewScreenshotTest") {
    val outputDir = layout.buildDirectory.dir("generated/screenshotTest/common/kotlin")
    val packageName = screenshotPackage
    inputs.property("packageName", packageName)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().asFile.resolve(packageName.replace('.', '/'))
            .resolve("PreviewScreenshotTest.kt")
        file.parentFile.mkdirs()
        file.writeText(
            """
            package $packageName

            import io.github.droidkaigi.confsched.core.testing.capturePreviews
            import kotlin.test.Test

            class PreviewScreenshotTest {
                @Test
                fun captureAllPreviews() {
                    capturePreviews(PreviewRegistry.previews)
                }
            }
            """.trimIndent(),
        )
    }
}

// The Android host-test task runs only the Roborazzi-generated preview tests: the shared
// robot/presenter tests in commonTest expect a plain JVM or native environment and fail under
// Robolectric, and they already run via jvmTest / iosSimulatorArm64Test.
tasks.withType<Test>().matching { it.name == "testAndroidHostTest" }.configureEach {
    filter.includeTestsMatching("com.github.takahirom.roborazzi.*")
    // Hardware pixel-copy rendering keeps the captured images faithful (the Roborazzi warning).
    systemProperty("robolectric.pixelCopyRenderMode", "hardware")
}

kotlin {
    android {
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets.named("commonTest") {
        kotlin.srcDir(generatePreviewScreenshotTest)
        dependencies {
            // The generated PreviewScreenshotTest is a kotlin.test class, so the module needs the
            // dependency whether or not it writes tests of its own.
            implementation(kotlin("test"))
            implementation(project(":core:testing"))
        }
    }

    sourceSets.named("androidHostTest") {
        dependencies {
            // Declared directly (not only via :core:testing) — the Roborazzi plugin verifies this
            // exact dependency on the module when generateComposePreviewRobolectricTests is on.
            implementation(libs.roborazziPreviewScannerSupport)
            implementation(libs.junit)
            implementation(libs.robolectric)
            implementation(libs.composablePreviewScannerAndroid)
            implementation(project(":core:testing"))
            implementation(libs.androidxActivityCompose)
            // Supplies the compileOnly impl classes (the preview image resolver binding that Metro
            // aggregates into KaigiPreviewWrapper's PreviewGraph) at screenshot-test runtime.
            implementation(project(":core:preview:impl"))
        }
    }
}
