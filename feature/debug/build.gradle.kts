plugins {
    alias(libs.plugins.droidkaigiConventionKmpFeature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:data"))
            implementation(project(":core:ui"))
            implementation(libs.ktorfitLib)
            implementation(libs.ktorClientCore)
            implementation(libs.jetwhaleAgentRuntime)
            implementation(libs.jetwhaleNav3Agent)
            implementation(libs.jetwhaleNetworkInspectorAgentKtor)
            implementation(libs.jetwhaleComposeSemanticsInspectorAgent)
            // Dev-only tooling: exempt from cross-feature isolation (never shipped).
            implementation(project(":feature:sessions"))
        }
    }
}
