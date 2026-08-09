package droidkaigi

import org.gradle.api.provider.Property

/** Configuration of `droidkaigi.primitive.licenses-export`. */
interface LicensesExportExtension {
    /** The Kotlin target whose resolved dependencies are exported. */
    val target: Property<String>

    /**
     * The source set the export is packaged with. Defaults to the target's own main source set;
     * iOS overrides it because its two targets share `iosMain`.
     */
    val sourceSet: Property<String>
}
