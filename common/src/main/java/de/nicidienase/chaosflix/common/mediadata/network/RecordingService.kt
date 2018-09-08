package de.nicidienase.chaosflix.common.mediadata.network

import de.nicidienase.chaosflix.common.mediadata.entities.recording.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.recording.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.Recording
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RecordingService {

    @GET("public/conferences")
    fun getConferencesWrapper(): Call<ConferencesWrapper>

    @GET("public/conferences/{id}")
    fun getConference(@Path("id") id: Long): Call<Conference>

    @GET("public/conferences/{name}")
    fun getConferenceByName(@Path("name") name: String): Call<Conference>

    @GET("public/events/{id}")
    fun getEvent(@Path("id") id: Long): Call<Event>

    @GET("public/events/{guid}")
    fun getEventByGUID(@Path("guid") guid: String): Call<Event>

    @GET("public/recordings/{id}")
    fun getRecording(@Path("id") id: Long): Call<Recording>

}