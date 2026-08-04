fun wrapper(content: () -> Unit) {
}

fun passesLambda(content: () -> Unit) {
    wrapper(content = <!LAMBDA_CAN_BE_PASSED_DIRECTLY!>{ content() }<!>)
}

fun passesValueDirectly(content: () -> Unit) {
    wrapper(content = content)
}

fun addsBehaviour(content: () -> Unit) {
    wrapper {
        content()
        content()
    }
}
