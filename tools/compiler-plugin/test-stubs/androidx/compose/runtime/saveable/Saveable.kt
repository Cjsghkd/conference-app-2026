package androidx.compose.runtime.saveable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import kotlinx.serialization.KSerializer

@Composable
inline fun <reified T : Any> rememberSerializable(vararg inputs: Any?, noinline init: () -> T): T = init()

@Composable
inline fun <reified T : Any> rememberSerializable(
    vararg inputs: Any?,
    noinline init: () -> MutableState<T>,
): MutableState<T> = init()

@Composable
fun <T : Any> rememberSerializable(vararg inputs: Any?, serializer: KSerializer<T>, init: () -> T): T = init()

@Composable
fun <T : Any> rememberSerializable(
    vararg inputs: Any?,
    stateSerializer: KSerializer<T>,
    init: () -> MutableState<T>,
): MutableState<T> = init()
