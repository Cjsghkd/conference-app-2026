import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.droidkaigiPrimitiveKmp)
    alias(libs.plugins.kotlinxSerialization)
    id("com.google.devtools.ksp")
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
        commonMain.dependencies {
            api(libs.kotlinxCollectionsImmutable)
            api(libs.soilQueryCore)
            implementation(libs.kotlinxSerializationJson)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":tools:ksp-processor"))
}

tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
