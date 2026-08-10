package io.github.droidkaigi.confsched.core.preview

import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.Language
import io.github.droidkaigi.confsched.core.model.Room
import io.github.droidkaigi.confsched.core.model.Timetable
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

fun Timetable.Companion.fake(): Timetable = Timetable(
    items = persistentListOf(
        fakeItem("d1a", "Sample Session A", Room.NARWHAL, "", Language.MIXED, DroidKaigi2026Day.Day1, "10:00", "10:20"),
        TimetableItem.fake(),
        fakeItem("d1c", "サンプルセッションC", Room.PANDA, "Speaker C", Language.ENGLISH, DroidKaigi2026Day.Day1, "11:00", "11:40"),
        fakeItem("d1d", "Sample Session D, with a placeholder title long enough to wrap onto several lines", Room.QUAIL, "Speaker D", Language.ENGLISH, DroidKaigi2026Day.Day1, "13:00", "13:45"),
        fakeItem("d1e", "サンプルセッションE、折り返しを確かめるための長いプレースホルダーのタイトル", Room.MEERKAT, "Speaker E", Language.ENGLISH, DroidKaigi2026Day.Day1, "13:00", "13:45"),
        fakeItem("d2a", "Sample Session F", Room.OTTER, "Speaker F", Language.MIXED, DroidKaigi2026Day.Day2, "10:00", "10:40"),
        fakeItem("d2b", "Sample Session G, with a moderately long placeholder title", Room.NARWHAL, "Speaker A", Language.MIXED, DroidKaigi2026Day.Day2, "11:00", "11:40"),
    ),
    bookmarks = persistentSetOf(TimetableItemId("d1a"), TimetableItemId("d1b"), TimetableItemId("d2a")),
)

fun TimetableItem.Companion.fake(): TimetableItem = fakeItem(
    id = "d1b",
    title = "サンプルセッションB",
    room = Room.OTTER,
    speaker = "Speaker B",
    language = Language.ENGLISH,
    day = DroidKaigi2026Day.Day1,
    startsAt = "11:00",
    endsAt = "11:40",
)

private fun fakeItem(
    id: String,
    title: String,
    room: Room,
    speaker: String,
    language: Language,
    day: DroidKaigi2026Day,
    startsAt: String,
    endsAt: String,
) = TimetableItem(
    id = TimetableItemId(id),
    title = title,
    room = room,
    speaker = speaker,
    language = language,
    day = day,
    startsAt = startsAt,
    endsAt = endsAt,
)
