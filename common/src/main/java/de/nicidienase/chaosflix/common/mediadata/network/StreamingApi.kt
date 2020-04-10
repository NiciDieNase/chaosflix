package de.nicidienase.chaosflix.common.mediadata.network

import de.nicidienase.chaosflix.BuildConfig
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import retrofit2.Response
import retrofit2.http.GET

interface StreamingApi {

    @GET(BuildConfig.STREAMING_API_OFFERS_PATH)
    suspend fun getStreamingConferences(): Response<List<LiveConference>>
}
