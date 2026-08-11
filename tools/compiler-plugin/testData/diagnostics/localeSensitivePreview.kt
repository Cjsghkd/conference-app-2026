import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper
import io.github.droidkaigi.confsched.core.preview.LocalePreviews
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// Mirrors the accessor Compose Resources generates for a `values/strings.xml` entry.
private object Res {
    object string {
        val session_title: StringResource = StringResource()
    }
}

@Composable
fun SessionTitle() {
    Text(stringResource(Res.string.session_title))
}

@Composable
fun SessionSubtitle(subtitle: StringResource) {
    Text(stringResource(subtitle))
}

<!LOCALE_SENSITIVE_PREVIEW_REQUIRES_EVERY_LOCALE!>@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun SingleLocalePreview() {
    SessionTitle()
}<!>

<!LOCALE_SENSITIVE_PREVIEW_REQUIRES_EVERY_LOCALE!>@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun SingleLocaleParameterPreview() {
    SessionSubtitle(StringResource())
}<!>

@LocalePreviews
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun EveryLocalePreview() {
    SessionTitle()
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun LocaleIndependentPreview() {
    Text("session")
}

// @LocalePreviews carries @Preview, so the wrapper rule reaches a preview declared through it.
<!PREVIEW_WITHOUT_WRAPPER!>@LocalePreviews
@Composable
private fun EveryLocalePreviewWithoutWrapper() {
    SessionTitle()
}<!>
