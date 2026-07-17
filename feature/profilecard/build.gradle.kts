plugins {
    id("droidkaigi.convention.kmp-feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:ui"))
        }
    }
}
