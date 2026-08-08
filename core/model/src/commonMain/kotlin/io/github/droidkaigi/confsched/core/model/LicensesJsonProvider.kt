package io.github.droidkaigi.confsched.core.model

/**
 * Reads the AboutLibraries exports of the running platform. Each entry module resolves a different
 * dependency graph, and a platform may resolve more than one: iOS collects its Kotlin dependencies
 * through Gradle and its Swift packages through Xcode.
 */
fun interface LicensesJsonProvider {
    suspend fun provide(): List<String>
}
