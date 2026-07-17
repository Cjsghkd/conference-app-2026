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
            implementation(libs.composeComponentsResources)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "io.github.droidkaigi.confsched.core.preview.impl.generated.resources"
    generateResClass = always
}

// Exposes the preview drawable directory so :core:preview:api can generate the PreviewImage enum
// from a declared dependency instead of reaching into this project's source layout.
val previewDrawables by configurations.consumable("previewDrawables")

artifacts {
    add(previewDrawables.name, layout.projectDirectory.dir("src/commonMain/composeResources/drawable"))
}
