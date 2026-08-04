package io.github.droidkaigi.confsched.feature.debug

// Soil's ErrorRecord has an internal constructor, so a preview cannot build one; the UI carries
// the two fields it renders instead.
data class SoilError(
    val keyId: String,
    val exception: Throwable,
)

internal fun previewSoilError() = SoilError(
    keyId = "TimetableQueryKey",
    exception = IllegalStateException("HTTP 503 Service Unavailable"),
)
