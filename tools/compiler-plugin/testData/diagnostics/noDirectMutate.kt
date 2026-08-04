import soil.query.compose.MutationObject

suspend fun callsMutate(mutation: MutationObject<Unit, String>) {
    mutation.<!NO_DIRECT_MUTATE!>mutate<!>("id")
}

suspend fun aliasesMutate(mutation: MutationObject<Unit, String>) {
    val alias = <!NO_DIRECT_MUTATE!>mutation.mutate<!>
    alias("id")
}

suspend fun callsMutateAsync(mutation: MutationObject<Unit, String>) {
    mutation.mutateAsync("id")
}
