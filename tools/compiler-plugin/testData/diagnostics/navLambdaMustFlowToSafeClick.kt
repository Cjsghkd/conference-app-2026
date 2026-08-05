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

// FILE: SearchResultRow.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper
import io.github.droidkaigi.confsched.core.ui.Modifier
import io.github.droidkaigi.confsched.core.ui.safeClickable

@Composable
internal fun SearchResultRow(title: String, onClick: () -> Unit) {
    Modifier.safeClickable(onClick)
    Text(title)
}

@Composable
internal fun SearchSlot(content: @Composable () -> Unit) {
    content()
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun SearchResultRowPreview() {
    SearchResultRow(title = "title", onClick = {})
    SearchSlot { Text("slot") }
}

// FILE: AdaptingSearchScreen.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper

@Composable
fun AdaptingSearchScreen(onItemClick: (String) -> Unit) {
    SearchResultRow(title = "title", onClick = { onItemClick("id") })
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun AdaptingSearchScreenPreview() {
    AdaptingSearchScreen(onItemClick = {})
}

// FILE: SlottedSearchScreen.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper

@Composable
fun SlottedSearchScreen(onItemClick: (String) -> Unit) {
    SearchSlot(content = { <!NAV_LAMBDA_MUST_FLOW_TO_SAFE_CLICK!>onItemClick<!>("id") })
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun SlottedSearchScreenPreview() {
    SlottedSearchScreen(onItemClick = {})
}
