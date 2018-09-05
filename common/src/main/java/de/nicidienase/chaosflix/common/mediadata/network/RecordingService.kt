package de.nicidienase.chaosflix.common.mediadata.network

import de.nicidienase.chaosflix.common.mediadata.entities.recording.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.recording.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.Recording
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

public interface RecordingService {

    @GET("public/conferences")
    fun getConferencesWrapper(): Single<ConferencesWrapper>

    @GET("public/events")
    fun getAllEvents(): Single<List<Event>>

    @GET("public/conferences/{id}")
    fun getConference(@Path("id") id: Long): Single<Conference>

    @GET("public/conferences/{id}")
    fun getConferenceString(@Path("id") id: Long): Single<String>

    @GET("public/conferences/{name}")
    fun getConferenceByname(@Path("name") name: String): Single<Conference>

    @GET("public/events/{id}")
    fun getEvent(@Path("id") id: Long): Single<Event>

    @GET("public/recordings/{id}")
    fun getRecording(@Path("id") id: Long): Single<Recording>

}
