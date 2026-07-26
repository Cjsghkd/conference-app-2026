plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // compileOnly (never bundled) and the version must match the consumer's Kotlin, else the plugin fails to load with NoSuchMethodError/LinkageError.
    compileOnly(libs.kotlinCompilerEmbeddable)
}
