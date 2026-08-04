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
