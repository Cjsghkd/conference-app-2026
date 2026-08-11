plugins {
    alias(libs.plugins.droidkaigiPrimitiveKmp)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
}

kotlin {
    // The KMP convention derives the namespace from the project path, which is not a valid package here.
    android.namespace = "io.github.droidkaigi.confsched.jetwhale.protocol"

    sourceSets {
        commonMain.dependencies {
            api(libs.jetwhaleProtocolCore)
            implementation(libs.kotlinxSerializationJson)
        }
    }
}
