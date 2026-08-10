import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
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
