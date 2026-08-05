// FILE: SearchCard.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun <!UI_COMPONENT_WITHOUT_PREVIEW!>SearchCard<!>() {
    Text("card")
}

// FILE: SearchRow.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper

@Composable
fun SearchRow() {
    Text("row")
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun SearchRowPreview() {
    SearchRow()
}

// FILE: SearchSyncEffect.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable

@Composable
fun SearchSyncEffect() {
}

// FILE: SearchScreenRoot.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.ScreenContext

class SearchScreenContext : ScreenContext

context(_: SearchScreenContext)
@Composable
fun SearchScreenRoot() {
}
