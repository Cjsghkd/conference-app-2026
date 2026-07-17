package io.github.droidkaigi.confsched.core.data

import de.jensklingenberg.ktorfit.http.GET

@ProvidedApi
interface SessionsApi {
    @GET("events/droidkaigi2026/timetable")
    suspend fun getTimetable(): SessionsAllResponse
}
