plugins {
    alias(libs.plugins.droidkaigiPrimitiveKmp)
    alias(libs.plugins.droidkaigiPrimitiveKmpCompose)
    alias(libs.plugins.metro)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:preview:api"))
            implementation(project(":core:designsystem"))
            implementation(libs.composeRuntime)
            implementation(libs.composeUi)
            implementation(libs.composeUiToolingPreview)
            // Metro aggregates the resolver binding from impl at this module's compile time, while impl
            // stays off production classpaths; partial linkage tolerates the dangling reference because
            // Wrap never runs in production.
            compileOnly(project(":core:preview:impl"))
        }

        androidMain.dependencies {
            compileOnly(project(":core:preview:impl"))
        }
    }
}
