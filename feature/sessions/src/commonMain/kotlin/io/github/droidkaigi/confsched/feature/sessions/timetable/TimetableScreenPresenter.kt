package io.github.droidkaigi.confsched.feature.sessions.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import io.github.droidkaigi.confsched.core.common.ActionEffect
import io.github.droidkaigi.confsched.core.common.MutationErrorEffect
import io.github.droidkaigi.confsched.core.common.ScreenChannel
import io.github.droidkaigi.confsched.core.common.toUserMessage
import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.Timetable
import io.github.droidkaigi.confsched.core.model.TimetableItem
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import soil.query.compose.rememberMutation

@Composable
context(presenterContext: TimetablePresenterContext)
fun timetableScreenPresenter(
    screenChannel: ScreenChannel<TimetableScreenAction, TimetableScreenActionResult>,
    timetable: Timetable,
): TimetableScreenUiState {
    val favoriteMutation = rememberMutation(presenterContext.favoriteTimetableItemIdMutationKey)
    var selectedDay by retain { mutableStateOf(DroidKaigi2026Day.Day1) }

    ActionEffect(screenChannel) { action ->
        when (action) {
            is TimetableScreenAction.Bookmark -> favoriteMutation.mutateAsync(action.id)
            is TimetableScreenAction.SelectDay -> selectedDay = action.day
        }
    }

    MutationErrorEffect(favoriteMutation) { error ->
        screenChannel.emit(TimetableScreenActionResult.ShowMessage(error.toUserMessage()))
        favoriteMutation.reset()
    }

    return TimetableScreenUiState(
        day = selectedDay,
        timeSlots = timetable.itemsOn(selectedDay).toTimeSlots(),
        bookmarks = timetable.bookmarks,
    )
}

private fun PersistentList<TimetableItem>.toTimeSlots(): PersistentList<TimetableTimeSlot> =
    groupBy { it.startsAt to it.endsAt }
        .map { (time, items) ->
            TimetableTimeSlot(startsAt = time.first, endsAt = time.second, items = items.toPersistentList())
        }
        .toPersistentList()
