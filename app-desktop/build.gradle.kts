import droidkaigi.includeDebugFeature
import droidkaigi.isTaskRequested

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.droidkaigiPrimitiveKmpCompose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
}

val includeDebugFeature = project.includeDebugFeature(developmentBuild = project.isTaskRequested("run"))

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":app-shared"))
            if (includeDebugFeature) implementation(project(":feature:debug"))
            implementation(compose.desktop.currentOs)
            implementation(libs.okio)
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.droidkaigi.confsched.app.MainKt"
    }
}
