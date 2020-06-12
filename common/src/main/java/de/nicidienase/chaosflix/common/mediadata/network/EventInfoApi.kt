package de.nicidienase.chaosflix.common.mediadata.network

import de.nicidienase.chaosflix.common.mediadata.entities.eventinfo.dto.EventInfoWrapperDto
import retrofit2.http.GET

interface EventInfoApi {

    @GET("/eventkalender/events.json")
    suspend fun getVocEvents(): EventInfoWrapperDto
}
