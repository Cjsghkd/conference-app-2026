package kotlinx.coroutines

interface Job

interface CoroutineScope

fun CoroutineScope.launch(block: suspend () -> Unit): Job = throw UnsupportedOperationException()
