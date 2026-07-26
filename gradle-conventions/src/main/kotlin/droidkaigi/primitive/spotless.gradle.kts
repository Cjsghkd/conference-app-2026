package droidkaigi.primitive

import util.libs
import util.version

plugins {
    id("com.diffplug.spotless")
}

val ktlintVersion = libs.version("ktlint")

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint(ktlintVersion)
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(ktlintVersion)
    }
}
