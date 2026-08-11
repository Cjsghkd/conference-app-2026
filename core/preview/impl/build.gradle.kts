plugins {
    alias(libs.plugins.droidkaigiPrimitiveKmp)
    alias(libs.plugins.droidkaigiPrimitiveKmpCompose)
    alias(libs.plugins.metro)
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:preview:api"))
            implementation(project(":core:designsystem"))
            implementation(libs.composeRuntime)
            implementation(libs.composeUi)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
}

// Exposes the preview drawable directory so :core:preview:api can generate the PreviewImage enum
// from a declared dependency instead of reaching into this project's source layout.
val previewDrawables by configurations.consumable("previewDrawables")

artifacts {
    add(previewDrawables.name, layout.projectDirectory.dir("src/commonMain/composeResources/drawable"))
}
