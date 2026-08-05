plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetwhaleHost)
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // The JetWhale host supplies these at runtime; bundling them would clash with its own copies.
    compileOnly(libs.jetwhaleHostSdk)
    compileOnly(libs.jetwhaleHostComposeRuntime)
    compileOnly(libs.jetwhaleHostComposeFoundation)
    compileOnly(libs.jetwhaleHostComposeUi)
    compileOnly(libs.jetwhaleHostComposeMaterial3)
    compileOnly(libs.kotlinxSerializationJson)
    api(project(":tools:jetwhale-plugin:protocol"))
}

jetwhalePlugin {
    hostVersion.set(libs.versions.jetwhale)
    // The module is called `host`, which would name the jar after its role rather than its contents
    // once it sits among other plugin jars in ~/.jetwhale/plugins/.
    pluginArchiveName.set("conference-app-2026")
}
