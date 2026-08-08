package androidx.compose.runtime

import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KProperty

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

@Composable
fun <T> remember(key1: Any?, calculation: () -> T): T = calculation()

@Composable
fun rememberCoroutineScope(): CoroutineScope = throw UnsupportedOperationException()

@Composable
fun <T> rememberUpdatedState(newValue: T): MutableState<T> = mutableStateOf(newValue)

interface MutableState<T> {
    var value: T
}

operator fun <T> MutableState<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value

operator fun <T> MutableState<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this.value = value
}

fun <T> mutableStateOf(value: T): MutableState<T> = throw UnsupportedOperationException()

class CompositionLocal<T>

class ProvidedValue<T>

infix fun <T> CompositionLocal<T>.provides(value: T): ProvidedValue<T> = ProvidedValue()

@Composable
fun CompositionLocalProvider(vararg values: ProvidedValue<*>, content: @Composable () -> Unit) {
}
