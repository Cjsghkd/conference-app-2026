import io.github.droidkaigi.confsched.core.common.MustBeSerializable
import kotlinx.serialization.Serializable

fun <@MustBeSerializable RESPONSE : Any> buildPersistedQueryKey(persistKey: String, fetch: () -> RESPONSE): String =
    persistKey

@Serializable
class SerializableResponse

class PlainResponse

fun serializable(): String = buildPersistedQueryKey("key") { SerializableResponse() }

fun notSerializable(): String =
    <!PERSISTED_KEY_TYPE_NOT_SERIALIZABLE!>buildPersistedQueryKey("key") { PlainResponse() }<!>
