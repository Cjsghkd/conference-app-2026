@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, ExperimentalEncodingApi::class)

package io.github.droidkaigi.confsched.core.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private fun ByteArray.toNSData(): NSData =
    if (isEmpty()) {
        NSData()
    } else {
        usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
        }
    }

private fun NSData.toByteArray(): ByteArray {
    val len = length.toInt()
    if (len == 0) return ByteArray(0)
    val out = ByteArray(len)
    out.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
    }
    return out
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class LocalFileStorage : FileStorage {
    private val dir: String = run {
        val docs = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
            .firstOrNull() as? String
            ?: "."
        "$docs/droidkaigi-confsched/blobs"
    }
    private val fileManager = NSFileManager.defaultManager

    private fun ensureDir() {
        fileManager.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
    }

    private fun pathFor(key: String): String {
        val safe = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode(key.encodeToByteArray())
        return "$dir/$safe.bin"
    }

    override suspend fun get(key: String): ByteArray? = withContext(Dispatchers.Default) {
        NSData.dataWithContentsOfFile(pathFor(key))?.toByteArray()
    }

    override suspend fun put(key: String, bytes: ByteArray) = withContext(Dispatchers.Default) {
        ensureDir()
        bytes.toNSData().writeToFile(pathFor(key), atomically = true)
        Unit
    }

    override suspend fun delete(key: String) = withContext(Dispatchers.Default) {
        fileManager.removeItemAtPath(pathFor(key), error = null)
        Unit
    }

    override suspend fun clear() = withContext(Dispatchers.Default) {
        fileManager.removeItemAtPath(dir, error = null)
        Unit
    }
}
