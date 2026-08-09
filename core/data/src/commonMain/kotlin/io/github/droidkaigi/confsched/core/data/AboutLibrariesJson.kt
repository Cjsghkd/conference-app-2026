package io.github.droidkaigi.confsched.core.data

import com.mikepenz.aboutlibraries.Libs

// A library present in more than one export is kept once: iOS reports the Kotlin and the Swift side
// separately, and nothing stops a dependency from reaching the app through both.
internal fun List<String>.toLibs(): Libs {
    val exports = map { Libs.Builder().withJson(it).build() }
    return Libs(
        libraries = exports.flatMap { it.libraries }.distinctBy { it.uniqueId }.sortedBy { it.name.lowercase() },
        licenses = exports.flatMap { it.licenses }.toSet(),
    )
}
