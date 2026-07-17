package droidkaigi.primitive

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

val libs = the<LibrariesForLibs>()

plugins.withId("com.android.kotlin.multiplatform.library") {
    // Android Studio's preview renderer loads ComposeViewAdapter from ui-tooling on the android runtime classpath (see the compose-previews KMP docs); this plugin has no debugImplementation configuration.
    dependencies { "androidRuntimeClasspath"(libs.composeUiTooling) }

    // Workaround for Android previews crashing with an NPE when accessing compose resources
    // from another module: https://youtrack.jetbrains.com/issue/CMP-7170
    // (the AGP KMP library plugin names the variant "AndroidMain" instead of "Debug")
    tasks.configureEach {
        if (name == "packageAndroidMainResources") {
            dependsOn("copyAndroidMainComposeResourcesToAndroidAssets")
        }
    }
}

// Shared Kotlin configuration for every module (KMP primitives don't apply to single-target app
// modules like app-desktop, so both primitives carry it; re-applying is idempotent).
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
        jvmToolchain(21)
        compilerOptions {
            optIn.add("soil.query.annotation.ExperimentalSoilQueryApi")
        }
    }
}
