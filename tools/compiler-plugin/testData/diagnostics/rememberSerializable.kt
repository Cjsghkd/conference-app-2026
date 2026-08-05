// FILE: SearchState.kt
package io.github.droidkaigi.confsched.feature.search

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
class SerializableState

class PlainState

enum class SearchMode { All, Favorites }

val plainStateSerializer: KSerializer<PlainState> = throw UnsupportedOperationException()

// FILE: SearchScreen.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSerializable

@Composable
fun serializableState(): MutableState<SerializableState> = rememberSerializable { mutableStateOf(SerializableState()) }

@Composable
fun builtInState(): MutableState<String> = rememberSerializable { mutableStateOf("") }

@Composable
fun builtInContainerState(): MutableState<List<SerializableState>> =
    rememberSerializable { mutableStateOf(listOf(SerializableState())) }

@Composable
fun enumState(): MutableState<SearchMode> = rememberSerializable { mutableStateOf(SearchMode.All) }

@Composable
fun explicitSerializerState(): MutableState<PlainState> =
    rememberSerializable(stateSerializer = plainStateSerializer) { mutableStateOf(PlainState()) }

@Composable
fun plainState(): MutableState<PlainState> =
    <!REMEMBER_SERIALIZABLE_TYPE_NOT_SERIALIZABLE!>rememberSerializable { mutableStateOf(PlainState()) }<!>

@Composable
fun plainContainerState(): MutableState<List<PlainState>> =
    <!REMEMBER_SERIALIZABLE_TYPE_NOT_SERIALIZABLE!>rememberSerializable { mutableStateOf(listOf(PlainState())) }<!>
