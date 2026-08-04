import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiSchemeProvider

@Composable
fun ThemedCard() {
    MaterialTheme.colorScheme
    Text("card")
}

<!THEME_SENSITIVE_PREVIEW_REQUIRES_MULTI_THEME!>@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun SingleThemePreview() {
    ThemedCard()
}<!>

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun MultiThemePreview(@PreviewParameter(provider = KaigiSchemeProvider::class) colorScheme: ColorScheme) {
    ThemedCard()
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun ThemeIndependentPreview() {
    Text("card")
}
