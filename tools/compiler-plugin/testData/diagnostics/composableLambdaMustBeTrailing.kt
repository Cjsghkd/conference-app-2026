import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.provides

val LocalContentColor = CompositionLocal<Int>()

@Composable
fun Wrapper(label: String, content: @Composable () -> Unit) {
    content()
}

@Composable
fun LeadingSlot(icon: @Composable () -> Unit, label: String) {
    icon()
}

@Composable
fun PlainSlot(content: () -> Unit) {
    content()
}

@Composable
fun NamedLambda() {
    CompositionLocalProvider(LocalContentColor provides 0, content = <!COMPOSABLE_LAMBDA_MUST_BE_TRAILING!>{ Text("body") }<!>)
}

@Composable
fun TrailingLambda() {
    CompositionLocalProvider(LocalContentColor provides 0) {
        Text("body")
    }
}

@Composable
fun NamedArgumentAfterLambda() {
    Wrapper(content = <!COMPOSABLE_LAMBDA_MUST_BE_TRAILING!>{ Text("body") }<!>, label = "label")
}

@Composable
fun PassesValueThrough(content: @Composable () -> Unit) {
    Wrapper(label = "label", content = content)
}

@Composable
fun NonLastParameter() {
    LeadingSlot(icon = { Text("icon") }, label = "label")
}

@Composable
fun NonComposableFunctionType() {
    PlainSlot(content = { })
}
