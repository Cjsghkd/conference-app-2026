package soil.query

open class MutationId<T, S>(val namespace: String, vararg val tags: Any)

open class QueryId<T>(val namespace: String, vararg val tags: Any)

open class SubscriptionId<T>(val namespace: String, vararg val tags: Any)

interface MutationReceiver

interface MutationKey<T, S> {
    val id: MutationId<T, S>
    val mutate: suspend MutationReceiver.(variable: S) -> T
}

fun <T, S> buildMutationKey(
    id: MutationId<T, S>,
    mutate: suspend MutationReceiver.(variable: S) -> T,
): MutationKey<T, S> = throw UnsupportedOperationException()

interface QueryKey<T> {
    val id: QueryId<T>
}

fun <T> buildQueryKey(id: QueryId<T>, fetch: suspend () -> T): QueryKey<T> =
    throw UnsupportedOperationException()

interface MutationRef<T, S> {
    suspend fun mutate(variable: S): T

    suspend fun mutateAsync(variable: S)

    suspend fun reset()
}
