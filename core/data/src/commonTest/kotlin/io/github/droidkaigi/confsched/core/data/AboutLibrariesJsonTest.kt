package io.github.droidkaigi.confsched.core.data

import kotlin.test.Test
import kotlin.test.assertEquals

class AboutLibrariesJsonTest {

    private fun export(vararg libraries: String, licenseHash: String = "Apache-2.0") = """
        {
          "libraries": [
            ${libraries.joinToString(",") { library(it, licenseHash) }}
          ],
          "licenses": {
            "$licenseHash": { "name": "$licenseHash", "url": "https://example.com/$licenseHash" }
          }
        }
    """.trimIndent()

    private fun library(uniqueId: String, licenseHash: String) = """
        {
          "uniqueId": "$uniqueId",
          "artifactVersion": "1.0.0",
          "name": "${uniqueId.substringAfter(':')}",
          "developers": [],
          "licenses": ["$licenseHash"],
          "funding": []
        }
    """.trimIndent()

    @Test
    fun libraries_from_every_export_end_up_in_one_list() {
        val libs = listOf(
            export("com.example:library-b"),
            export("swiftpm:library-a", licenseHash = "Zlib"),
        ).toLibs()

        assertEquals(listOf("library-a", "library-b"), libs.libraries.map { it.name })
    }

    @Test
    fun the_merged_list_is_ordered_by_name_across_exports() {
        val libs = listOf(
            export("com.example:zebra", "com.example:alpha"),
            export("swiftpm:mango", licenseHash = "Zlib"),
        ).toLibs()

        assertEquals(listOf("alpha", "mango", "zebra"), libs.libraries.map { it.name })
    }

    @Test
    fun a_library_reaching_the_app_through_two_exports_is_listed_once() {
        val libs = listOf(
            export("com.example:library-a"),
            export("com.example:library-a"),
        ).toLibs()

        assertEquals(1, libs.libraries.size)
    }

    @Test
    fun every_export_contributes_its_licenses() {
        val libs = listOf(
            export("com.example:library-a"),
            export("swiftpm:library-b", licenseHash = "Zlib"),
        ).toLibs()

        assertEquals(setOf("Apache-2.0", "Zlib"), libs.licenses.map { it.name }.toSet())
    }

    @Test
    fun a_single_export_still_produces_the_libraries_it_carries() {
        assertEquals(1, listOf(export("com.example:library-a")).toLibs().libraries.size)
    }
}
