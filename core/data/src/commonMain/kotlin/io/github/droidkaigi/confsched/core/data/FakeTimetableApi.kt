package io.github.droidkaigi.confsched.core.data

import kotlinx.coroutines.delay

class FakeTimetableApi : TimetableApi {
    override suspend fun getTimetable(): TimetableResponse {
        delay(300)
        return TimetableResponse(
            status = HttpStatusResponse.OK,
            sessions = listOf(
                fakeSession("s1", "Sample Session A", 81669L, "sp1", LanguageResponse.JAPANESE, "2026-09-02T10:00:00+09:00", "2026-09-02T10:40:00+09:00"),
                fakeSession("s2", "Sample Session B, with a placeholder title long enough to wrap onto several lines", 81667L, "sp2", LanguageResponse.ENGLISH, "2026-09-02T11:00:00+09:00", "2026-09-02T11:40:00+09:00"),
                fakeSession("s3", "Sample Session C", 81669L, "sp3", LanguageResponse.MIXED, "2026-09-03T10:00:00+09:00", "2026-09-03T10:40:00+09:00"),
                fakeSession("s4", "Sample Session D, with a moderately long placeholder title", 81667L, "sp1", LanguageResponse.ENGLISH, "2026-09-03T11:00:00+09:00", "2026-09-03T11:40:00+09:00"),
            ),
            rooms = listOf(
                RoomResponse(name = LocaledResponse(ja = "Narwhal", en = "Narwhal"), id = 81669L, sort = 1),
                RoomResponse(name = LocaledResponse(ja = "Otter", en = "Otter"), id = 81667L, sort = 2),
            ),
            speakers = listOf(
                fakeSpeaker("sp1", "Speaker A"),
                fakeSpeaker("sp2", "Speaker B"),
                fakeSpeaker("sp3", "Speaker C"),
            ),
            categories = emptyList(),
        )
    }

    private fun fakeSession(
        id: String,
        title: String,
        roomId: Long,
        speakerId: String,
        language: LanguageResponse,
        startsAt: String,
        endsAt: String,
    ) = SessionResponse(
        id = id,
        title = LocaledResponse(ja = title, en = title),
        speakers = listOf(speakerId),
        startsAt = startsAt,
        endsAt = endsAt,
        language = language,
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
