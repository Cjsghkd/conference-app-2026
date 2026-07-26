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
    // Not the convention plugin: on the root classpath it breaks the other convention plugins.
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
            .matching { it.name.startsWith("kotlinCompilerPluginClasspath") }
            .configureEach {
                dependencies.add(project.dependencies.project(mapOf("path" to ":tools:compiler-plugin")))
            }
    }
}
