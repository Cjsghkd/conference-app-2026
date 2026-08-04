import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class ServerEnvironmentStore {
    private val mutableEnvironment = MutableStateFlow("Staging")
    val <!PROPERTY_MUST_USE_EXPLICIT_BACKING_FIELD!>environment<!>: StateFlow<String> = mutableEnvironment.asStateFlow()
}

class PlainMirrorStore {
    private val mutableEnvironment = MutableStateFlow("Staging")
    val <!PROPERTY_MUST_USE_EXPLICIT_BACKING_FIELD!>environment<!>: StateFlow<String> = mutableEnvironment
}

class BackingFieldStore {
    val environment: StateFlow<String>
        field = MutableStateFlow("Staging")
}

class DerivedStore {
    private val mutableEnvironment = MutableStateFlow("Staging")
    val environment: Flow<String> = mutableEnvironment.map { it }
}

class ConstructorStore(private val mutableEnvironment: MutableStateFlow<String>) {
    val environment: StateFlow<String> = mutableEnvironment.asStateFlow()
}
