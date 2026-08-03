package io.github.droidkaigi.confsched.feature.favorites

import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.FavoriteTimetableItemIdMutationKey
import io.github.droidkaigi.confsched.core.model.Timetable
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import io.github.droidkaigi.confsched.core.testing.runPresenterTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import soil.query.MutationId
import soil.query.buildMutationKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FavoritesScreenPresenterTest {

    private val sampleTimetable = Timetable(
        items = persistentListOf(
            TimetableItem(TimetableItemId("d1a"), "Day1 A", "Room1", "Sp1", DroidKaigi2026Day.Day1, "10:00", "10:40"),
            TimetableItem(TimetableItemId("d1b"), "Day1 B", "Room2", "Sp2", DroidKaigi2026Day.Day1, "11:00", "11:40"),
            TimetableItem(TimetableItemId("d2a"), "Day2 A", "Room1", "Sp3", DroidKaigi2026Day.Day2, "10:00", "10:40"),
            TimetableItem(TimetableItemId("d2b"), "Day2 B", "Room2", "Sp4", DroidKaigi2026Day.Day2, "11:00", "11:40"),
        ),
        bookmarks = persistentSetOf(TimetableItemId("d1a"), TimetableItemId("d2a"), TimetableItemId("d2b")),
    )

    @Test
    fun initial_state_lists_only_favorited_items_grouped_by_day_and_time() {
        val favoriteKey: FavoriteTimetableItemIdMutationKey = buildMutationKey(
            id = MutationId("test-favorite"),
            mutate = { },
        )
        runPresenterTest(
            presenterContext = FavoritesPresenterContext(favoriteTimetableItemIdMutationKey = favoriteKey),
            presenter = { channel -> favoritesScreenPresenter(screenChannel = channel, timetable = sampleTimetable) },
        ) {
            val initial = uiStates.awaitItem()
            assertEquals(null, initial.selectedDayFilter)

            val slots = initial.favoritesListSection.timeSlots
            assertEquals(
                listOf(
                    Triple(DroidKaigi2026Day.Day1, "10:00", "10:40"),
                    Triple(DroidKaigi2026Day.Day2, "10:00", "10:40"),
                    Triple(DroidKaigi2026Day.Day2, "11:00", "11:40"),
                ),
                slots.map { slot -> Triple(slot.day, slot.startsAt, slot.endsAt) },
            )
            assertEquals(listOf("d1a"), slots[0].items.map { it.id.value })
            assertEquals(listOf("d2a"), slots[1].items.map { it.id.value })
            assertEquals(listOf("d2b"), slots[2].items.map { it.id.value })
        }
    }

    @Test
    fun selecting_a_day_filter_narrows_the_list_to_that_day() {
        val favoriteKey: FavoriteTimetableItemIdMutationKey = buildMutationKey(
            id = MutationId("test-favorite-filter"),
            mutate = { },
        )
        runPresenterTest(
            presenterContext = FavoritesPresenterContext(favoriteTimetableItemIdMutationKey = favoriteKey),
            presenter = { channel -> favoritesScreenPresenter(screenChannel = channel, timetable = sampleTimetable) },
        ) {
            uiStates.awaitItem()

            send(FavoritesScreenAction.SelectDayFilter(DroidKaigi2026Day.Day2))
            val onDay2 = uiStates.awaitItem()
            assertEquals(DroidKaigi2026Day.Day2, onDay2.selectedDayFilter)
            assertEquals(
                listOf("d2a", "d2b"),
                onDay2.favoritesListSection.timeSlots.flatMap { slot -> slot.items.map { it.id.value } },
            )
        }
    }

    @Test
    fun bookmark_action_forwards_the_id_to_the_mutation() {
        val mutateInvocations = Channel<TimetableItemId>(Channel.UNLIMITED)
        val favoriteKey: FavoriteTimetableItemIdMutationKey = buildMutationKey(
            id = MutationId("test-favorite-bookmark"),
            mutate = { id -> mutateInvocations.trySend(id) },
        )
        runPresenterTest(
            presenterContext = FavoritesPresenterContext(favoriteTimetableItemIdMutationKey = favoriteKey),
            presenter = { channel -> favoritesScreenPresenter(screenChannel = channel, timetable = sampleTimetable) },
        ) {
            uiStates.awaitItem()

            send(FavoritesScreenAction.Bookmark(TimetableItemId("d1a")))
            assertEquals(TimetableItemId("d1a"), mutateInvocations.receive())
        }
    }

    @Test
    fun mutation_failure_surfaces_ShowMessage_on_channel() {
        val failingKey: FavoriteTimetableItemIdMutationKey = buildMutationKey(
            id = MutationId("test-favorite-failing"),
            mutate = { _ -> error("boom") },
        )
        runPresenterTest(
            presenterContext = FavoritesPresenterContext(favoriteTimetableItemIdMutationKey = failingKey),
            presenter = { channel -> favoritesScreenPresenter(screenChannel = channel, timetable = sampleTimetable) },
        ) {
            uiStates.awaitItem()
            send(FavoritesScreenAction.Bookmark(TimetableItemId("d1a")))

            val result = results.awaitItem()
            assertIs<FavoritesScreenActionResult.ShowMessage>(result)
            assertEquals("boom", result.message.text)
        }
    }
}
