import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    // No Compose code lives here, but the Compose Gradle plugin syncs the compose resources of the
    // whole dependency graph into the app bundle, and it hangs that off the Xcode entry point of
    // the module declaring the Swift Export binary — this one.
    alias(libs.plugins.droidkaigiPrimitiveKmpCompose)
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
}

kotlin {
    jvmToolchain(21)

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        iosMain.dependencies {
            implementation(project(":app-shared"))
        }
    }

    @OptIn(ExperimentalSwiftExportDsl::class)
    swiftExport {
        moduleName.set("AppShared")
        flattenPackage.set("io.github.droidkaigi.confsched.app.ios")
    }
}
