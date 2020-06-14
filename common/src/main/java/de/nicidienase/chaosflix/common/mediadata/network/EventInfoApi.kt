package de.nicidienase.chaosflix.common.mediadata.network

import de.nicidienase.chaosflix.common.mediadata.entities.eventinfo.dto.EventInfoWrapperDto
import retrofit2.http.GET
import retrofit2.http.Query

interface EventInfoApi {

    @GET("/eventkalender/events.json")
    suspend fun getVocEvents(
            @Query("filter") filter: String = "upcoming", // {past|upcoming|today|2013}
            @Query("streaming") streaming: String = "true" // {true|false|undefined}
    ): EventInfoWrapperDto
}
