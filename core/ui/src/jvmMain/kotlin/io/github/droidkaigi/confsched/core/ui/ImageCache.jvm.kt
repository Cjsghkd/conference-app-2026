package io.github.droidkaigi.confsched.core.ui

import coil3.PlatformContext
import coil3.disk.DiskCache
import java.io.File
import okio.Path.Companion.toOkioPath

internal actual fun imageDiskCache(context: PlatformContext): DiskCache? =
    DiskCache.Builder()
        .directory(File(System.getProperty("java.io.tmpdir"), "droidkaigi2026/image_cache").toOkioPath())
        .build()
