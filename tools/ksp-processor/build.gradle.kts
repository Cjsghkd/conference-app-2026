plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.kspSymbolProcessingApi)
    implementation(libs.kotlinPoetKsp)
}
