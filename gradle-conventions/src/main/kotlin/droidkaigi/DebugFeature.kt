package droidkaigi

import org.gradle.api.Project

// The fallback must stay `developmentBuild` and never `true`: a build that cannot identify itself
// has to lose the debug screen, which shows up in development, rather than ship it unnoticed.
fun Project.includeDebugFeature(developmentBuild: Boolean): Boolean =
    (findProperty("includeDebugFeature") as String?)?.toBoolean() ?: developmentBuild

fun Project.isTaskRequested(taskName: String): Boolean =
    gradle.startParameter.taskNames.any { it.substringAfterLast(':') == taskName }
