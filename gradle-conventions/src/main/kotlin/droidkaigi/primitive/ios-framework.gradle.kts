package droidkaigi.primitive

import droidkaigi.LicensesExportExtension
import droidkaigi.includeDebugFeature
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

// The iOS half of the module that builds AppShared.framework. app-ios is an Xcode project rather
// than a Gradle module, so one shared module has to be the iOS entry as well; keeping that wiring
// here leaves its build script about the features it aggregates.
plugins {
    // Declared so the Kotlin DSL generates accessors for the multiplatform extension's nested
    // swiftExport / swiftPMDependencies blocks; the module applies it through its own primitive.
    id("org.jetbrains.kotlin.multiplatform")
    id("droidkaigi.primitive.licenses-export")
}

val libs = the<LibrariesForLibs>()

// embedAndSignAppleFrameworkForXcode inherits Xcode's CONFIGURATION; a Gradle-driven framework link has none.
val includeDebugFeature = project.includeDebugFeature(
    developmentBuild = providers.environmentVariable("CONFIGURATION").orNull.equals("Debug", ignoreCase = true),
)

@OptIn(ExperimentalSwiftExportDsl::class, ExperimentalKotlinGradlePluginApi::class)
configure<KotlinMultiplatformExtension> {
    targets.withType(KotlinNativeTarget::class.java).configureEach {
        binaries.framework {
            baseName = "AppShared"
            isStatic = false
            // Exported so its declarations keep their own names; an unexported dependency
            // carries its module name into the generated ones.
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
        // Exported so the module's types surface to Swift as first-class types rather than
        // opaque Kotlin references.
        export(project(":core:common")) {
            flattenPackage.set("io.github.droidkaigi.confsched.core.common")
        }
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
