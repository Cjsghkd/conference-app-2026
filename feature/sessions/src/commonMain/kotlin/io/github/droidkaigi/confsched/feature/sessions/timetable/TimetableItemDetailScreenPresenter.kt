package io.github.droidkaigi.confsched.feature.sessions.timetable

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.ActionEffect
import io.github.droidkaigi.confsched.core.common.MutationErrorEffect
import io.github.droidkaigi.confsched.core.common.ScreenChannel
import io.github.droidkaigi.confsched.core.common.toUserMessage
import io.github.droidkaigi.confsched.core.model.TimetableItem
import soil.query.compose.rememberMutation

context(presenterContext: TimetableItemDetailPresenterContext)
@Composable
fun timetableItemDetailScreenPresenter(
    screenChannel: ScreenChannel<TimetableItemDetailScreenAction, TimetableItemDetailScreenActionResult>,
    item: TimetableItem,
    isFavorite: Boolean,
): TimetableItemDetailScreenUiState {
    val favoriteMutation = rememberMutation(presenterContext.favoriteTimetableItemIdMutationKey)

    ActionEffect(screenChannel) { action ->
        when (action) {
            is TimetableItemDetailScreenAction.Bookmark -> favoriteMutation.mutateAsync(action.id)
        }
    }

    MutationErrorEffect(favoriteMutation) { error ->
        screenChannel.emit(TimetableItemDetailScreenActionResult.ShowMessage(error.toUserMessage()))
        favoriteMutation.reset()
    }

    return TimetableItemDetailScreenUiState(item = item, isFavorite = isFavorite)
}
