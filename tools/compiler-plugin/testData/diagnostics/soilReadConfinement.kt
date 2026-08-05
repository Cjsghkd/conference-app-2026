// FILE: Contexts.kt
package io.github.droidkaigi.confsched.feature.search

import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenContext
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.buildMutationKey
import soil.query.buildQueryKey

class SearchPresenterContext : PresenterContext

class SearchScreenContext : ScreenContext

val searchQueryKey: QueryKey<String> = buildQueryKey(QueryId("search")) { "" }

val searchMutationKey: MutationKey<Unit, String> = buildMutationKey(MutationId("search")) { }

// FILE: SearchResultList.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper
import soil.query.compose.rememberQuery

@Composable
fun SearchResultList() {
    <!QUERY_READ_OUTSIDE_SCREEN_CONTEXT!>rememberQuery(searchQueryKey)<!>
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun SearchResultListPreview() {
    SearchResultList()
}

// FILE: SearchScreenRoot.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import soil.query.compose.rememberQuery

context(_: SearchScreenContext)
@Composable
fun SearchScreenRoot() {
    rememberQuery(searchQueryKey)
}

// FILE: SearchResultRow.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import io.github.droidkaigi.confsched.core.preview.KaigiPreviewWrapper
import soil.query.compose.rememberMutation

@Composable
fun SearchResultRow() {
    <!MUTATION_READ_OUTSIDE_PRESENTER_CONTEXT!>rememberMutation(searchMutationKey)<!>
}

@Preview
@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)
@Composable
private fun SearchResultRowPreview() {
    SearchResultRow()
}

// FILE: SearchPresenter.kt
package io.github.droidkaigi.confsched.feature.search

import androidx.compose.runtime.Composable
import soil.query.MutationRef
import soil.query.compose.rememberMutation

context(_: SearchPresenterContext)
@Composable
fun searchPresenter() {
    rememberMutation(searchMutationKey)
}

suspend fun mutateOutsidePresenter(mutation: MutationRef<Unit, String>) {
    <!MUTATION_CALL_OUTSIDE_PRESENTER_CONTEXT!>mutation.mutateAsync("id")<!>
}

context(_: SearchPresenterContext)
suspend fun mutateInsidePresenter(mutation: MutationRef<Unit, String>) {
    mutation.mutateAsync("id")
}
