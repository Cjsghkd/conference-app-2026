package droidkaigi.primitive

import droidkaigi.LicensesExportExtension
import droidkaigi.includeDebugFeature
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// The iOS half of the module the Swift Export module builds on. app-ios is an Xcode project rather
// than a Gradle module, so one shared module has to carry the iOS-only wiring; keeping it here
// leaves the build script about the features it aggregates.
plugins {
    // Declared so the Kotlin DSL generates accessors for the multiplatform extension's nested
    // swiftPMDependencies block; the module applies it through its own primitive.
    id("org.jetbrains.kotlin.multiplatform")
    id("droidkaigi.primitive.licenses-export")
}

val libs = the<LibrariesForLibs>()

// embedSwiftExportForXcode inherits Xcode's CONFIGURATION; a Gradle-driven compilation has none.
val includeDebugFeature = project.includeDebugFeature(
    developmentBuild = providers.environmentVariable("CONFIGURATION").orNull.equals("Debug", ignoreCase = true),
)

@OptIn(ExperimentalKotlinGradlePluginApi::class)
configure<KotlinMultiplatformExtension> {
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("16.0")
        swiftPackage(
            url = "https://github.com/firebase/firebase-ios-sdk",
            version = "12.0.0",
            products = listOf("FirebaseCrashlytics"),
            importedClangModules = listOf("FirebaseCrashlytics", "FirebaseCore"),
        )
    }

    // configureEach, not named: the default hierarchy materialises iosMain after this plugin applies.
    sourceSets.configureEach {
        if (name != "iosMain") return@configureEach
        dependencies {
            implementation(libs.okio)
            // Metro aggregates the debug feature from the iosMain compile classpath, so the dependency must sit here.
            if (includeDebugFeature) implementation(project(":feature:debug"))
        }
    }
}

// Compose resources carry one directory per source set and the two iOS targets share iosMain, so one
// of them has to stand for both: they compile the same source set and therefore resolve the same
// libraries, and the device target is the one that ships. A simulator build shows the same list,
// differing only in the artifact suffix of the coordinates that carry one.
configure<LicensesExportExtension> {
    target = "iosArm64"
    sourceSet = "iosMain"
}
