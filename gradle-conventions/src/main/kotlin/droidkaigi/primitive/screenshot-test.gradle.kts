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
        // Previews are private: nothing but the tooling and this scan ever calls one.
        includePrivatePreviews.set(true)
        robolectricConfig.set(
            mapOf(
                "sdk" to "[36]",
                "qualifiers" to "\"w360dp-h800dp-xhdpi\"",
            ),
        )
    }
}

// The Android host-test task renders: the Roborazzi-generated preview tests, and the Robot tests
// that capture the states a preview cannot reach. Allow-listed rather than excluding, so a kind of
// test added later has to be let in deliberately — the presenter tests, for one, want a plain JVM
// and already run via jvmTest / iosSimulatorArm64Test.
tasks.withType<Test>().matching { it.name == "testAndroidHostTest" }.configureEach {
    filter.includeTestsMatching("com.github.takahirom.roborazzi.*")
    filter.includeTestsMatching("*RobotTest")
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
        dependencies {
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
