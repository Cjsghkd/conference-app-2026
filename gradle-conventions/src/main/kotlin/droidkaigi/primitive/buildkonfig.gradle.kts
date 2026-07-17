package droidkaigi.primitive

import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.codingfeline.buildkonfig")
}

val libs = the<LibrariesForLibs>()

buildkonfig {
    packageName = "io.github.droidkaigi.confsched"

    defaultConfigs {
        buildConfigField(STRING, "versionName", libs.versions.droidkaigiApp.get())
    }
}
