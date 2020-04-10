package de.nicidienase.chaosflix.common.mediadata.network

import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferenceDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.recording.EventDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.EventsResponse
import de.nicidienase.chaosflix.common.mediadata.entities.recording.RecordingDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecordingService {

    @GET("public/conferences")
    suspend fun getConferencesWrapper(): Response<ConferencesWrapper>

    @GET("public/conferences/{id}")
    suspend fun getConference(@Path("id") id: Long): Response<ConferenceDto>

    @GET("public/conferences/{name}")
    suspend fun getConferenceByName(@Path("name") name: String): Response<ConferenceDto>

    @GET("public/events/{id}")
    suspend fun getEvent(@Path("id") id: Long): Response<EventDto>

    @GET("public/events/{guid}")
    suspend fun getEventByGUID(@Path("guid") guid: String): EventDto?

    @GET("public/events/search")
    suspend fun searchEvents(@Query("q") query: String): Response<EventsResponse>

    @GET("public/recordings/{id}")
    suspend fun getRecording(@Path("id") id: Long): Response<RecordingDto>

    @GET("public/conferences")
    suspend fun getConferencesWrapperSuspending(): Response<ConferencesWrapper?>

    @GET("public/conferences/{acronym}")
    suspend fun getConferenceByNameSuspending(@Path("acronym")acronym: String): Response<ConferenceDto?>

    @GET("public/events/search")
    suspend fun searchEventsSuspending(@Query("q") query: String): Response<EventsResponse?>

    @GET("public/events/{guid}")
    suspend fun getEventByGUIDSuspending(@Path("guid") guid: String): Response<EventDto?>
}
