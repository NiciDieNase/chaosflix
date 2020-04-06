package de.nicidienase.chaosflix.common.mediadata.network

import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferenceDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.recording.EventDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.EventsResponse
import de.nicidienase.chaosflix.common.mediadata.entities.recording.RecordingDto
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecordingService {

    @GET("public/conferences")
    fun getConferencesWrapper(): Call<ConferencesWrapper>

    @GET("public/conferences/{id}")
    fun getConference(@Path("id") id: Long): Call<ConferenceDto>

    @GET("public/conferences/{name}")
    fun getConferenceByName(@Path("name") name: String): Call<ConferenceDto>

    @GET("public/events/{id}")
    fun getEvent(@Path("id") id: Long): Call<EventDto>

    @GET("public/events/{guid}")
    suspend fun getEventByGUID(@Path("guid") guid: String): EventDto?

    @GET("public/events/search")
    suspend fun searchEvents(@Query("q") query: String): EventsResponse

    @GET("public/recordings/{id}")
    fun getRecording(@Path("id") id: Long): Call<RecordingDto>

    @GET("public/conferences")
    suspend fun getConferencesWrapperSuspending(): ConferencesWrapper?

    @GET("public/conferences/{acronym}")
    suspend fun getConferenceByNameSuspending(@Path("acronym")acronym: String): Response<ConferenceDto?>

    @GET("public/events/search")
    suspend fun searchEventsSuspending(@Query("q") query: String): EventsResponse?

    @GET("public/events/{guid}")
    suspend fun getEventByGUIDSuspending(@Path("guid") guid: String): EventDto?
}
