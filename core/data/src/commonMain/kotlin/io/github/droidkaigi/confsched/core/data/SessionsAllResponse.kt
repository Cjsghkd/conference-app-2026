package io.github.droidkaigi.confsched.core.data

import io.github.droidkaigi.confsched.core.model.DroidKaigi2026Day
import io.github.droidkaigi.confsched.core.model.TimetableItem
import io.github.droidkaigi.confsched.core.model.TimetableItemId
import kotlinx.serialization.Serializable

@Serializable
data class SessionsAllResponse(
    val sessions: List<SessionResponse> = emptyList(),
    val rooms: List<RoomResponse> = emptyList(),
    val speakers: List<SpeakerResponse> = emptyList(),
    val categories: List<CategoryResponse> = emptyList(),
)

@Serializable
data class SessionResponse(
    val id: String,
    val isServiceSession: Boolean,
    val title: LocaledResponse,
    val speakers: List<String>,
    val description: String? = null,
    val i18nDesc: LocaledResponse? = null,
    val startsAt: String,
    val endsAt: String,
    val language: String,
    val roomId: Int,
    val sessionCategoryItemId: Int,
    val sessionType: String,
    val message: SessionMessageResponse? = null,
    val isPlenumSession: Boolean,
    val targetAudience: String,
    val interpretationTarget: Boolean,
    val asset: SessionAssetResponse,
    val levels: List<String> = emptyList(),
)

@Serializable
data class LocaledResponse(
    val ja: String? = null,
    val en: String? = null,
)

@Serializable
data class RoomResponse(
    val name: LocaledResponse,
    val id: Int,
    val sort: Int,
)

@Serializable
data class SpeakerResponse(
    val profilePicture: String? = null,
    val sessions: List<Int> = emptyList(),
    val tagLine: String? = null,
    val isTopSpeaker: Boolean? = null,
    val bio: String? = null,
    val fullName: String,
    val id: String,
)

@Serializable
data class CategoryResponse(
    val id: Int,
    val sort: Int,
    val title: LocaledResponse,
    val items: List<CategoryItemResponse> = emptyList(),
)

@Serializable
data class CategoryItemResponse(
    val name: LocaledResponse,
    val id: Int,
    val sort: Int,
)

@Serializable
data class SessionMessageResponse(
    val ja: String,
    val en: String,
)

@Serializable
data class SessionAssetResponse(
    val videoUrl: String? = null,
    val slideUrl: String? = null,
)

fun SessionsAllResponse.toTimetableItems(): List<TimetableItem> {
    val roomNameById = rooms.associateBy({ it.id }, { it.name.ja ?: it.name.en ?: "" })
    val speakerNameById = speakers.associateBy({ it.id }, { it.fullName })
    // Conference days are not encoded in the payload; the two distinct dates map to Day1/Day2.
    val dayByDate = sessions.map { it.startsAt.date() }.distinct().sorted()
        .mapIndexed { index, date -> date to DroidKaigi2026Day.entries[index.coerceAtMost(DroidKaigi2026Day.entries.lastIndex)] }
        .toMap()
    return sessions
        .sortedBy { it.startsAt }
        .map { session ->
            TimetableItem(
                id = TimetableItemId(session.id),
                title = session.title.ja ?: session.title.en ?: "",
                room = roomNameById[session.roomId] ?: "",
                speaker = session.speakers.mapNotNull { speakerNameById[it] }.joinToString(", "),
                day = dayByDate.getValue(session.startsAt.date()),
                startsAt = session.startsAt.time(),
                endsAt = session.endsAt.time(),
            )
        }
}

// Timestamps arrive as ISO-8601 with offset ("2026-09-10T10:00:00+09:00", wall-clock JST).
private fun String.date(): String = substringBefore('T')
private fun String.time(): String = substringAfter('T').take(5)
