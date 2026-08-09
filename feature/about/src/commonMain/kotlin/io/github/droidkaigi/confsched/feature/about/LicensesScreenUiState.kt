package io.github.droidkaigi.confsched.feature.about

import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Developer
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.entity.Scm

data class LicensesScreenUiState(
    val libs: Libs,
) {
    companion object
}

internal fun LicensesScreenUiState.Companion.fake(): LicensesScreenUiState = LicensesScreenUiState(
    libs = Libs(
        libraries = listOf(
            fakeLibrary("library-a", "Library A", "1.0.0"),
            fakeLibrary("library-b", "Library B", "2.3.1"),
        ),
        licenses = setOf(FakeLicense),
    ),
)

private val FakeLicense = License(
    name = "Apache-2.0",
    url = "https://example.com/apache-2.0",
    spdxId = "Apache-2.0",
    hash = "apache-2.0",
)

private fun fakeLibrary(id: String, name: String, version: String) = Library(
    uniqueId = "com.example:$id",
    artifactVersion = version,
    name = name,
    description = "An example library.",
    website = "https://example.com/$id",
    developers = listOf(Developer(name = "Developer A", organisationUrl = null)),
    organization = null,
    scm = Scm(connection = null, developerConnection = null, url = "https://example.com/$id"),
    licenses = setOf(FakeLicense),
)
