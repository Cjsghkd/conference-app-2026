plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.droidkaigiPrimitiveKmpCompose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
}

val includeDebugFeature = (project.findProperty("includeDebugFeature") as String?)?.toBoolean() ?: true

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":app-shared"))
            implementation(libs.androidxDatastoreCoreOkio)
            if (includeDebugFeature) implementation(project(":feature:debug"))
        }
    }
}
