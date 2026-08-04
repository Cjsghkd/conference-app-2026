import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewTheme
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper

<!PREVIEW_WITHOUT_WRAPPER!>@Preview
@Composable
private fun UnwrappedPreview() {
    Text("search")
}<!>

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun WrappedPreview() {
    Text("search")
}

@Preview
@Composable
private fun OwnThemePreview() {
    KaigiPreviewTheme(androidx.compose.material3.ColorScheme()) {
        Text("search")
    }
}
