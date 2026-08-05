package io.github.droidkaigi.confsched.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class ItemId(val value: String)

class ItemUiState(val id: ItemId, val title: String)

@Composable
fun ReportsItsOwnParameter(
    id: ItemId,
    <!NO_CALLER_SUPPLIED_CALLBACK_ARGUMENT!>onClick<!>: (ItemId) -> Unit,
) {
    onClick(id)
}

@Composable
fun ReportsAPropertyOfItsOwnParameter(
    uiState: ItemUiState,
    <!NO_CALLER_SUPPLIED_CALLBACK_ARGUMENT!>onClick<!>: (ItemId) -> Unit,
) {
    onClick(uiState.id)
}

@Composable
fun ReportsFromWithinALambda(
    id: ItemId,
    <!NO_CALLER_SUPPLIED_CALLBACK_ARGUMENT!>onClick<!>: (ItemId) -> Unit,
) {
    val handler = remember { { onClick(id) } }
    handler()
}

@Composable
fun TakesNoArgumentCallback(
    id: ItemId,
    onClick: () -> Unit,
) {
    Label(id.value, onClick)
}

@Composable
fun ReportsAnElementOfAList(
    uiStates: List<ItemUiState>,
    onClick: (ItemId) -> Unit,
) {
    uiStates.forEach { uiState -> onClick(uiState.id) }
}

@Composable
fun ReportsInternalState(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val draft = remember { query }
    onQueryChange(draft)
}

@Composable
fun ReportsATransformedValue(
    count: Int,
    onCountChange: (Int) -> Unit,
) {
    onCountChange(count + 1)
}

@Composable
fun ForwardsTheCallbackOn(
    id: ItemId,
    onClick: (ItemId) -> Unit,
) {
    ReportsAPropertyOfItsOwnParameter(ItemUiState(id, "title"), onClick)
}

@Composable
fun FillsAContentSlot(
    uiState: ItemUiState,
    content: @Composable (ItemUiState) -> Unit,
) {
    content(uiState)
}

@Composable
fun Label(text: String, onClick: () -> Unit) {
}
