package de.nicidienase.chaosflix.common.mediadata.network

import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.http.GET

public interface StreamingService {

    @GET("streams/v2.json")
    fun getStreamingConferences(): Single<List<LiveConference>>
}
