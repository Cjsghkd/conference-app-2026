import io.github.droidkaigi.confsched.core.common.MustBeSerializable
import kotlinx.serialization.Serializable

fun <@MustBeSerializable RESPONSE : Any> buildPersistedQueryKey(persistKey: String, fetch: () -> RESPONSE): String =
    persistKey

@Serializable
class SerializableResponse

class PlainResponse

enum class ResponseKind { Cached, Fresh }

fun serializable(): String = buildPersistedQueryKey("key") { SerializableResponse() }

fun builtIn(): String = buildPersistedQueryKey("key") { "response" }

fun builtInContainer(): String = buildPersistedQueryKey("key") { listOf(SerializableResponse()) }

fun builtInPair(): String = buildPersistedQueryKey("key") { 1 to listOf("response") }

fun enumResponse(): String = buildPersistedQueryKey("key") { ResponseKind.Fresh }

fun notSerializable(): String =
    <!PERSISTED_KEY_TYPE_NOT_SERIALIZABLE!>buildPersistedQueryKey("key") { PlainResponse() }<!>

fun notSerializableInsideContainer(): String =
    <!PERSISTED_KEY_TYPE_NOT_SERIALIZABLE!>buildPersistedQueryKey("key") { listOf(PlainResponse()) }<!>
