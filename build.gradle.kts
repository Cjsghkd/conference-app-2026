plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
    }
}

subprojects {
    if (path != ":tools:compiler-plugin" && path != ":tools:ksp-processor") {
        configurations
            // Swift Export's bridge sources are generated, not written here, and the enforcement
            // checkers reject house-style violations the generator is free to emit.
            .matching { it.name.startsWith("kotlinCompilerPluginClasspath") && !it.name.contains("SwiftExport") }
            .configureEach {
                // shadowRuntimeElements carries the relocated jar; the plain one references
                // com.intellij, which the embeddable compiler loading this plugin does not have.
                dependencies.add(
                    project.dependencies.project(
                        mapOf(
                            "path" to ":tools:compiler-plugin",
                            "configuration" to "shadowRuntimeElements",
                        ),
                    ),
                )
            }
    }
}
