package de.nicidienase.chaosflix.common.mediadata.network

import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import retrofit2.Response
import retrofit2.http.GET

interface StreamingApi {

    @GET("/streams/v2.json")
    suspend fun getStreamingConferences(): Response<List<LiveConference>>
}
