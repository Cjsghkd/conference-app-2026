package androidx.compose.material3

import androidx.compose.runtime.Composable

class ColorScheme

class Typography

class Shapes

object MaterialTheme {
    val colorScheme: ColorScheme
        @Composable get() = ColorScheme()

    val typography: Typography
        @Composable get() = Typography()

    val shapes: Shapes
        @Composable get() = Shapes()
}

class PaddingValues

@Composable
fun Text(text: String) {
}

@Composable
fun Button(onClick: () -> Unit, content: @Composable () -> Unit) {
}

@Composable
fun Card(content: @Composable () -> Unit) {
}

@Composable
fun Column(content: @Composable () -> Unit) {
}

@Composable
fun Row(content: @Composable () -> Unit) {
}

@Composable
fun Scaffold(content: @Composable (PaddingValues) -> Unit) {
}

class LazyListScope {
    fun <T> items(items: List<T>, itemContent: @Composable (T) -> Unit) {
    }
}

@Composable
fun LazyColumn(content: LazyListScope.() -> Unit) {
}
