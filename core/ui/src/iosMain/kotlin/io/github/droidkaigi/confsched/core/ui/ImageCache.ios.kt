package io.github.droidkaigi.confsched.core.ui

import coil3.PlatformContext
import coil3.disk.DiskCache
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal actual fun imageDiskCache(context: PlatformContext): DiskCache? {
    val cachesDirectory = NSSearchPathForDirectoriesInDomains(
        directory = NSCachesDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).firstOrNull() as? String ?: return null
    return DiskCache.Builder()
        .directory("$cachesDirectory/image_cache".toPath())
        .build()
}
