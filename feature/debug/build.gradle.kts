plugins {
    alias(libs.plugins.droidkaigiConventionKmpFeature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:data"))
            implementation(project(":core:ui"))
            implementation(libs.ktorfitLib)
            // Dev-only tooling: exempt from cross-feature isolation (never shipped).
            implementation(project(":feature:sessions"))
        }
    }
}
