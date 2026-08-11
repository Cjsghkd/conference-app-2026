plugins {
    alias(libs.plugins.droidkaigiConventionKmpFeature)
    // Bakes this machine's address into the buildMachineWss() call in JetWhaleDebugger. The address
    // is a compile input, so this module recompiles when the developer changes networks — keep the
    // plugin on the one module that declares the endpoint.
    alias(libs.plugins.jetwhaleAgent)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:data"))
            implementation(project(":core:ui"))
            implementation(libs.kotlinxDatetime)
            implementation(libs.ktorfitLib)
            implementation(libs.ktorClientCore)
            implementation(project(":tools:jetwhale-plugin:protocol"))
            implementation(libs.jetwhaleAgentRuntime)
            implementation(libs.jetwhaleAgentSdk)
            implementation(libs.jetwhaleNav3Agent)
            implementation(libs.jetwhaleNetworkInspectorAgentKtor)
            implementation(libs.jetwhaleComposeSemanticsInspectorAgent)
            // Dev-only tooling: exempt from cross-feature isolation (never shipped).
            implementation(project(":feature:sessions"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
