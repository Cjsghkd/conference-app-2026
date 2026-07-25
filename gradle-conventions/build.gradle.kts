import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

// kotlin-dsl compiles with Gradle's embedded Kotlin, which rejects the plugin markers' 2.4.0 metadata by default; skip that check.
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

fun pluginMarker(id: String, version: String) = "$id:$id.gradle.plugin:$version"

dependencies {
    val kotlinVersion = libs.versions.kotlin.get()
    implementation(pluginMarker("org.jetbrains.kotlin.multiplatform", kotlinVersion))
    implementation(pluginMarker("org.jetbrains.kotlin.plugin.compose", kotlinVersion))
    implementation(pluginMarker("org.jetbrains.kotlin.plugin.serialization", kotlinVersion))
    implementation(pluginMarker("org.jetbrains.compose", libs.versions.jetbrainsCompose.get()))
    implementation(pluginMarker("com.android.kotlin.multiplatform.library", libs.versions.agp.get()))
    implementation(pluginMarker("dev.zacsweers.metro", libs.versions.metro.get()))
    implementation(pluginMarker("com.google.devtools.ksp", libs.versions.ksp.get()))
    implementation(pluginMarker("com.codingfeline.buildkonfig", libs.versions.buildkonfig.get()))
    implementation(pluginMarker("io.github.takahirom.roborazzi", libs.versions.roborazzi.get()))
    implementation(pluginMarker("org.openapi.generator", libs.versions.openapiGenerator.get()))
    implementation(libs.kaml)

    // Expose the generated version-catalog accessors (the<LibrariesForLibs>()) to the precompiled script plugins.
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
