// FILE: TimetableScreen.kt
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper

@Composable
fun TimetableScreen() {
    Text("timetable")
}

@Composable
private fun <!SCREEN_FILE_DECLARES_EXTRA_COMPONENT!>TimetableCard<!>() {
    Text("card")
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun TimetableScreenPreview() {
    TimetableScreen()
}

// FILE: TimetableCard.kt
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun TimetableCardComponent() {
    Text("card")
}
