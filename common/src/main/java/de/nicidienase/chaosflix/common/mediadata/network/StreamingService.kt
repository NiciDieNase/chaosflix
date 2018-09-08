package de.nicidienase.chaosflix.common.mediadata.network

import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import retrofit2.Call
import retrofit2.http.GET

interface StreamingService {

    @GET("streams/v2.json")
    fun getStreamingConferences(): Call<List<LiveConference>>
}
