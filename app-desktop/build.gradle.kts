import droidkaigi.includeDebugFeature
import droidkaigi.isAnyTaskRequested

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.droidkaigiPrimitiveKmpCompose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.droidkaigiPrimitiveLicensesExport)
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
}

// Besides `run`, Compose Hot Reload contributes its own launchers; `jvmRunHot` is its deprecated alias for `hotRunJvm`.
val includeDebugFeature = project.includeDebugFeature(
    developmentBuild = project.isAnyTaskRequested(
        "run",
        "hotRunJvm",
        "hotRunJvmAsync",
        "hotDevJvm",
        "hotDevJvmAsync",
        "jvmRunHot",
    ),
)

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
