class Navigator {
    fun openSessionDetail(id: String) {
    }
}

fun open(onNavigateToDetail: (String) -> Unit) {
}

val navigator = Navigator()

fun forwardsParameter() {
    open(<!LAMBDA_CAN_BE_CALLABLE_REFERENCE!>{ id -> navigator.openSessionDetail(id) }<!>)
}

fun usesCallableReference() {
    open(navigator::openSessionDetail)
}

fun addsBehaviour() {
    open { id -> navigator.openSessionDetail(id.trim()) }
}
