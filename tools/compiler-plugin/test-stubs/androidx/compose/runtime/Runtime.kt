package androidx.compose.runtime

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER,
    AnnotationTarget.VALUE_PARAMETER,
)
@Retention(AnnotationRetention.BINARY)
annotation class Composable

@Composable
fun LaunchedEffect(key: Any?, block: suspend () -> Unit) {
}

@Composable
fun <T> remember(calculation: () -> T): T = calculation()

interface MutableState<T> {
    var value: T
}

fun <T> mutableStateOf(value: T): MutableState<T> = throw UnsupportedOperationException()

class CompositionLocal<T>

class ProvidedValue<T>

infix fun <T> CompositionLocal<T>.provides(value: T): ProvidedValue<T> = ProvidedValue()

@Composable
fun CompositionLocalProvider(vararg values: ProvidedValue<*>, content: @Composable () -> Unit) {
}
