package kotlinx.coroutines.flow

interface Flow<out T>

interface SharedFlow<out T> : Flow<T>

interface StateFlow<out T> : SharedFlow<T> {
    val value: T
}

interface MutableSharedFlow<T> : SharedFlow<T>

interface MutableStateFlow<T> :
    StateFlow<T>,
    MutableSharedFlow<T> {
    override var value: T
}

fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = throw UnsupportedOperationException()

fun <T> MutableSharedFlow(): MutableSharedFlow<T> = throw UnsupportedOperationException()

fun <T> MutableStateFlow<T>.asStateFlow(): StateFlow<T> = this

fun <T> MutableSharedFlow<T>.asSharedFlow(): SharedFlow<T> = this

fun <T, R> Flow<T>.map(transform: suspend (T) -> R): Flow<R> = throw UnsupportedOperationException()
