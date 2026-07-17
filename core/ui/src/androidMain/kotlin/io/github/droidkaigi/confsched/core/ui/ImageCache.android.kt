package io.github.droidkaigi.confsched.core.ui

import coil3.PlatformContext
import coil3.disk.DiskCache
import okio.Path.Companion.toOkioPath

internal actual fun imageDiskCache(context: PlatformContext): DiskCache? =
    DiskCache.Builder()
        .directory(context.cacheDir.resolve("image_cache").toOkioPath())
        .build()
