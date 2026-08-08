package droidkaigi

import org.gradle.api.Project

// The fallback must stay `developmentBuild` and never `true`: a build that cannot identify itself
// has to lose the debug screen, which shows up in development, rather than ship it unnoticed.
fun Project.includeDebugFeature(developmentBuild: Boolean): Boolean =
    providers.gradleProperty("includeDebugFeature").orNull?.toBoolean() ?: developmentBuild

fun Project.isAnyTaskRequested(vararg taskNames: String): Boolean =
    gradle.startParameter.taskNames.any { requested -> requested.substringAfterLast(':') in taskNames }
