package io.github.droidkaigi.confsched.core.common

enum class TargetPlatform {
    Android,
    Ios,
    Desktop,
    Web,
}

/**
 * Marks a declaration in a common source set that only has an effect on one platform.
 * The PlatformOnlyNaming checker requires the declaration name to start with the platform name
 * (for example `Ios...`), and conversely requires this annotation on any common top-level
 * declaration whose name starts with a platform prefix.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class PlatformOnly(val platform: TargetPlatform)
