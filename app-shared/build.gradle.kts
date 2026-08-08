@file:OptIn(ExperimentalSwiftExportDsl::class, ExperimentalKotlinGradlePluginApi::class)

import droidkaigi.includeDebugFeature
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

plugins {
    alias(libs.plugins.droidkaigiPrimitiveKmp)
    alias(libs.plugins.droidkaigiPrimitiveKmpCompose)
    alias(libs.plugins.droidkaigiPrimitiveBuildkonfig)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
}

// embedAndSignAppleFrameworkForXcode inherits Xcode's CONFIGURATION; a Gradle-driven framework link has none.
val includeDebugFeature = project.includeDebugFeature(
    developmentBuild = providers.environmentVariable("CONFIGURATION").orNull.equals("Debug", ignoreCase = true),
)

kotlin {
    android {
        namespace = "io.github.droidkaigi.confsched.app.shared"
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "AppShared"
            isStatic = false
            // Exported so Swift sees core:common types (CrashReporter) without the module-name prefix.
            export(project(":core:common"))
        }
    }

    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("16.0")
        swiftPackage(
            url = "https://github.com/firebase/firebase-ios-sdk",
            version = "12.0.0",
            products = listOf("FirebaseCrashlytics"),
            importedClangModules = listOf("FirebaseCrashlytics", "FirebaseCore"),
        )
    }

    swiftExport {
        moduleName.set("AppShared")
        flattenPackage.set("io.github.droidkaigi.confsched.app")
        // Export :core:common so its navigation types reachable from the iOS shell surface
        // as first-class Swift types instead of opaque Kotlin references.
        export(project(":core:common")) {
            flattenPackage.set("io.github.droidkaigi.confsched.core.common")
        }
    }

    sourceSets {
        commonMain.dependencies {
            // api (not implementation): the platform app modules inherit these so Metro aggregates every feature/core contribution into the graph.
            api(project(":core:model"))
            api(project(":core:common"))
            api(project(":core:data"))
            api(project(":core:designsystem"))
            api(project(":core:ui"))
            api(project(":feature:sessions"))
            api(project(":feature:about"))
            api(project(":feature:contributors"))
            api(project(":feature:sponsors"))
            api(project(":feature:profilecard"))
            api(project(":feature:favorites"))
            api(project(":feature:eventmap"))
            implementation(libs.composeMaterial3AdaptiveNavigation3)
        }
        iosMain.dependencies {
            implementation(libs.okio)
            // Metro aggregates the debug feature from the iosMain compile classpath, so the dependency must sit here.
            if (includeDebugFeature) implementation(project(":feature:debug"))
        }
    }
}
