package soil.query.compose

import androidx.compose.runtime.Composable
import soil.query.MutationKey
import soil.query.QueryKey

interface QueryObject<out T> {
    val data: T?
}

interface SubscriptionObject<out T> {
    val data: T?
}

interface MutationObject<out T, S> {
    val data: T?
    val mutate: suspend (variable: S) -> T
    val mutateAsync: suspend (variable: S) -> Unit
    val reset: suspend () -> Unit
}

@Composable
fun <T> rememberQuery(key: QueryKey<T>): QueryObject<T> = throw UnsupportedOperationException()

@Composable
fun <T> rememberSubscription(key: QueryKey<T>): SubscriptionObject<T> =
    throw UnsupportedOperationException()

@Composable
fun <T, S> rememberMutation(key: MutationKey<T, S>): MutationObject<T, S> =
    throw UnsupportedOperationException()
