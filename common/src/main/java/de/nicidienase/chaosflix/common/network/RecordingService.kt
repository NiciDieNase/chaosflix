package de.nicidienase.chaosflix.common.network

import de.nicidienase.chaosflix.common.entities.recording.Conference
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.entities.recording.Event
import de.nicidienase.chaosflix.common.entities.recording.Recording
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by felix on 17.03.17.
 */

interface RecordingService {

    @GET("public/conferences")
    fun getConferences(): Observable<ConferencesWrapper>

    @GET("public/events")
    fun getAllEvents(): Observable<List<Event>>

    @GET("public/conferences/{id}")
    fun getConference(@Path("id") id: Long): Observable<Conference>

    @GET("public/events/{id}")
    fun getEvent(@Path("id") id: Long): Observable<Event>

    @GET("public/recordings/{id}")
    fun getRecording(@Path("id") id: Long): Observable<Recording>

}
