package io.github.droidkaigi.confsched.core.data

import kotlinx.coroutines.delay

class FakeSessionsApi : SessionsApi {
    override suspend fun getTimetable(): SessionsAllResponse {
        delay(300)
        return SessionsAllResponse(
            sessions = listOf(
                fakeSession("s1", "Kotlin 2.4 context parameters in anger", 1, "sp1", "2026-09-10T10:00:00+09:00", "2026-09-10T10:40:00+09:00"),
                fakeSession("s2", "Compose Multiplatform on Desktop", 2, "sp2", "2026-09-10T11:00:00+09:00", "2026-09-10T11:40:00+09:00"),
                fakeSession("s3", "Metro DI: graphs without Dagger", 1, "sp3", "2026-09-11T10:00:00+09:00", "2026-09-11T10:40:00+09:00"),
                fakeSession("s4", "Soil query/mutation patterns", 2, "sp1", "2026-09-11T11:00:00+09:00", "2026-09-11T11:40:00+09:00"),
            ),
            rooms = listOf(
                RoomResponse(name = LocaledResponse(ja = "Arctic Fox", en = "Arctic Fox"), id = 1, sort = 1),
                RoomResponse(name = LocaledResponse(ja = "Bumblebee", en = "Bumblebee"), id = 2, sort = 2),
            ),
            speakers = listOf(
                SpeakerResponse(fullName = "Alice", id = "sp1"),
                SpeakerResponse(fullName = "Bob", id = "sp2"),
                SpeakerResponse(fullName = "Carol", id = "sp3"),
            ),
        )
    }

    private fun fakeSession(
        id: String,
        title: String,
        roomId: Int,
        speakerId: String,
        startsAt: String,
        endsAt: String,
    ) = SessionResponse(
        id = id,
        isServiceSession = false,
        title = LocaledResponse(ja = title, en = title),
        speakers = listOf(speakerId),
        startsAt = startsAt,
        endsAt = endsAt,
        language = "JAPANESE",
        roomId = roomId,
        sessionCategoryItemId = 1,
        sessionType = "NORMAL",
        isPlenumSession = false,
        targetAudience = "All",
        interpretationTarget = false,
        asset = SessionAssetResponse(),
    )
}
