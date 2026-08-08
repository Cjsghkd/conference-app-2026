package droidkaigi.convention

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("droidkaigi.primitive.kmp")
    id("droidkaigi.primitive.kmp.compose")
    id("droidkaigi.primitive.screenshot-test")
    id("droidkaigi.primitive.spotless")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("dev.zacsweers.metro")
    id("com.google.devtools.ksp")
}

configure<KotlinMultiplatformExtension> {
    sourceSets.named("commonMain") {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        dependencies {
            // The only production reference is the @PreviewWrapper annotation argument
            // KaigiPreviewWrapper::class; the wrapper is instantiated only on preview/screenshot classpaths.
            implementation(project(":core:preview:wrapper"))
        }
    }
    sourceSets.named("androidMain") {
        dependencies {
            // compileOnly in androidMain: visible to Android Studio's @Preview compile classpath but never on any runtime classpath.
            compileOnly(project(":core:preview:impl"))
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":tools:ksp-processor"))
}

plugins.withId("com.android.kotlin.multiplatform.library") {
    // Preview images resolve from impl's composeResources on the IDE preview render classpath; impl stays compileOnly for production.
    dependencies { "androidRuntimeClasspath"(project(":core:preview:impl")) }
}

tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
