plugins {
    alias(libs.plugins.droidkaigiConventionKmpFeature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:ui"))
            implementation(libs.composeMaterial3AdaptiveNavigation3)
        }
        commonTest.dependencies {
            implementation(project(":core:model"))
            implementation(kotlin("test"))
            implementation(project(":core:testing"))
        }
    }
}
