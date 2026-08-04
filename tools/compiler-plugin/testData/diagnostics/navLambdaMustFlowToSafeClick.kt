// FILE: SearchScreen.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper

@Composable
fun SearchScreen(onItemClick: () -> Unit) {
    Button(onClick = <!NAV_LAMBDA_MUST_FLOW_TO_SAFE_CLICK!>onItemClick<!>) {
        Text("open")
    }
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun SearchScreenPreview() {
    SearchScreen(onItemClick = {})
}

// FILE: DebouncedSearchScreen.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper
import io.github.droidkaigi.confsched.core.ui.Modifier
import io.github.droidkaigi.confsched.core.ui.safeClickable

@Composable
fun DebouncedSearchScreen(onItemClick: () -> Unit) {
    Modifier.safeClickable(onItemClick)
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun DebouncedSearchScreenPreview() {
    DebouncedSearchScreen(onItemClick = {})
}

// FILE: ForwardingSearchScreen.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper

@Composable
fun ForwardingSearchScreen(onItemClick: () -> Unit) {
    DebouncedSearchScreen(onItemClick = onItemClick)
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun ForwardingSearchScreenPreview() {
    ForwardingSearchScreen(onItemClick = {})
}
