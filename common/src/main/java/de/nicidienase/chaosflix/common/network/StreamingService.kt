package de.nicidienase.chaosflix.common.network

import de.nicidienase.chaosflix.common.entities.streaming.LiveConference
import io.reactivex.Observable
import retrofit2.http.GET

/**
 * Created by felix on 23.03.17.
 */

interface StreamingService {

    @GET("streams/v2.json")
    fun getStreamingConferences(): Observable<List<LiveConference>>
}
