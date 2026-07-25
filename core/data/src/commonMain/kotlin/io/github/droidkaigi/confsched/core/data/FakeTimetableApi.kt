package io.github.droidkaigi.confsched.core.data

import kotlinx.coroutines.delay

class FakeTimetableApi : TimetableApi {
    override suspend fun getTimetable(): TimetableResponse {
        delay(300)
        return TimetableResponse(
            status = HttpStatusResponse.OK,
            sessions = listOf(
                fakeSession("s1", "Kotlin 2.4 context parameters in anger", 1L, "sp1", "2026-09-10T10:00:00+09:00", "2026-09-10T10:40:00+09:00"),
                fakeSession("s2", "Compose Multiplatform on Desktop", 2L, "sp2", "2026-09-10T11:00:00+09:00", "2026-09-10T11:40:00+09:00"),
                fakeSession("s3", "Metro DI: graphs without Dagger", 1L, "sp3", "2026-09-11T10:00:00+09:00", "2026-09-11T10:40:00+09:00"),
                fakeSession("s4", "Soil query/mutation patterns", 2L, "sp1", "2026-09-11T11:00:00+09:00", "2026-09-11T11:40:00+09:00"),
            ),
            rooms = listOf(
                RoomResponse(name = LocaledResponse(ja = "Arctic Fox", en = "Arctic Fox"), id = 1L, sort = 1),
                RoomResponse(name = LocaledResponse(ja = "Bumblebee", en = "Bumblebee"), id = 2L, sort = 2),
            ),
            speakers = listOf(
                fakeSpeaker("sp1", "Alice"),
                fakeSpeaker("sp2", "Bob"),
                fakeSpeaker("sp3", "Carol"),
            ),
            categories = emptyList(),
        )
    }

    private fun fakeSession(
        id: String,
        title: String,
        roomId: Long,
        speakerId: String,
        startsAt: String,
        endsAt: String,
    ) = SessionResponse(
        id = id,
        title = LocaledResponse(ja = title, en = title),
        speakers = listOf(speakerId),
        startsAt = startsAt,
        endsAt = endsAt,
        language = LanguageResponse.JAPANESE,
        roomId = roomId,
        lengthInMinutes = 40,
        sessionType = SessionTypeResponse.NORMAL,
        noShow = false,
        targetAudience = LocaledResponse(ja = "All", en = "All"),
        interpretationTarget = false,
        asset = SessionAssetResponse(),
    )

    private fun fakeSpeaker(id: String, fullName: String) = SpeakerResponse(
        id = id,
        fullName = fullName,
        tagLine = "",
        sessions = emptyList(),
    )
}
