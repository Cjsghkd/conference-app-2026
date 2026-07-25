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
        }

        // Metro aggregates the resolver binding from impl at this module's compile time, while impl
        // stays off production classpaths. Only the Android and JVM targets render previews, and
        // compileOnly is unsupported for Kotlin/Native and Kotlin/Wasm, so the dependency is declared
        // per target instead of in commonMain; the other targets fall back to NoopPreviewImageResolver.
        androidMain.dependencies {
            compileOnly(project(":core:preview:impl"))
        }

        jvmMain.dependencies {
            compileOnly(project(":core:preview:impl"))
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":core:preview:api"))
            // Supplies at runtime the impl classes that jvmMain sees only at compile time.
            implementation(project(":core:preview:impl"))
        }
    }
}
