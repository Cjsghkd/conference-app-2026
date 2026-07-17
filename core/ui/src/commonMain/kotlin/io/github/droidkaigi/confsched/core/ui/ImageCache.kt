package io.github.droidkaigi.confsched.core.ui

import coil3.PlatformContext
import coil3.disk.DiskCache

// Returns null on platforms without a filesystem (wasmJs), where only the memory cache applies.
internal expect fun imageDiskCache(context: PlatformContext): DiskCache?
