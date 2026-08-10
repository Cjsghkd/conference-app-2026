package droidkaigi.primitive

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME

// SwiftExportConstants.SWIFT_EXPORT_COMPILATION is internal to the Kotlin Gradle plugin.
val swiftExportCompilationName = "swiftExportMain"

// shadowRuntimeElements carries the relocated jar; the plain one references com.intellij, which the
// embeddable compiler loading this plugin does not have.
val checkers = dependencies.project(
    mapOf("path" to ":tools:compiler-plugin", "configuration" to "shadowRuntimeElements"),
)

// The Kotlin plugin creates the extension and the per-compilation plugin classpaths as it applies,
// so the module's own plugins block may name this primitive before or after the Kotlin one.
plugins.withType<KotlinBasePlugin>().configureEach {
    the<KotlinProjectExtension>().eachTarget { target ->
        target.compilations.configureEach {
            // Swift Export's bridge sources are generated, not written here, and the checkers reject
            // house-style violations the generator is free to emit.
            if (name == swiftExportCompilationName) return@configureEach
            project.dependencies.add(
                PLUGIN_CLASSPATH_CONFIGURATION_NAME + disambiguatedName.replaceFirstChar(Char::uppercaseChar),
                checkers,
            )
        }
    }
}

fun KotlinProjectExtension.eachTarget(action: (KotlinTarget) -> Unit) = when (this) {
    is KotlinMultiplatformExtension -> targets.configureEach(action)
    is KotlinSingleTargetExtension<*> -> action(target)
    else -> error("$path applies a Kotlin plugin whose extension exposes no target: $this")
}
