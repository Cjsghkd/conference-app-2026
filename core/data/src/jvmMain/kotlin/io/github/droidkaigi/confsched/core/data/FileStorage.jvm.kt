package io.github.droidkaigi.confsched.core.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Base64

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class LocalFileStorage : FileStorage {
    private val dir = File(System.getProperty("user.home"), ".droidkaigi-confsched/blobs")

    private fun fileFor(key: String): File {
        val safe = Base64.getUrlEncoder().withoutPadding().encodeToString(key.encodeToByteArray())
        return File(dir, "$safe.bin")
    }

    override suspend fun get(key: String): ByteArray? = withContext(Dispatchers.IO) {
        val f = fileFor(key)
        if (f.exists()) f.readBytes() else null
    }

    override suspend fun put(key: String, bytes: ByteArray) = withContext(Dispatchers.IO) {
        dir.mkdirs()
        fileFor(key).writeBytes(bytes)
    }

    override suspend fun delete(key: String) = withContext(Dispatchers.IO) {
        fileFor(key).delete()
        Unit
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        dir.listFiles()?.forEach { it.delete() }
        Unit
    }
}
