package droidkaigi.primitive

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        optIn.add("soil.query.annotation.ExperimentalSoilQueryApi")
    }

    jvm()

    android {
        namespace = "io.github.droidkaigi.confsched" + project.path.replace(":", ".")
        compileSdk = 37
        minSdk = 24
        // Required so composeResources are available as Android resources on the IDE preview classpath.
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    iosArm64()
    iosSimulatorArm64()
}
