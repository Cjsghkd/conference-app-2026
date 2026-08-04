package io.github.droidkaigi.confsched.core.common

import androidx.compose.runtime.Composable
import soil.query.compose.MutationObject

interface SoilDataContext

interface ScreenContext : SoilDataContext

interface PresenterContext

interface Navigator

class ScreenChannel<Action, ActionResult> {
    context(_: ScreenContext)
    fun send(action: Action) {
    }

    context(_: PresenterContext)
    suspend fun emit(result: ActionResult) {
    }
}

inline fun <A, R> context(context: A, block: context(A) () -> R): R = block(context)

@Composable
context(_: PresenterContext)
fun <A> ActionEffect(channel: ScreenChannel<A, *>, block: suspend (A) -> Unit) {
}

@Composable
context(_: ScreenContext)
fun <R> ActionResultEffect(channel: ScreenChannel<*, R>, block: suspend (R) -> Unit) {
}

@Composable
fun MutationSuccessEffect(mutation: MutationObject<*, *>, onSuccess: suspend (Any?) -> Unit) {
}

@Composable
fun MutationErrorEffect(mutation: MutationObject<*, *>, onError: suspend (Throwable) -> Unit) {
}

enum class TargetPlatform {
    Android,
    Ios,
    Desktop,
    Web,
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class PlatformOnly(val platform: TargetPlatform)

@Target(AnnotationTarget.TYPE_PARAMETER)
annotation class MustBeSerializable

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class ThemeSensitive
