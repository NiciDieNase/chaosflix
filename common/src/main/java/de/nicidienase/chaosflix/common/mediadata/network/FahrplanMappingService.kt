package de.nicidienase.chaosflix.common.mediadata.network

import retrofit2.http.GET

interface FahrplanMappingService {

    @GET("NiciDieNase/d8bbb9f7b73efddd0cf6e6c4aa93f3ba/raw/02f8604bac4a9a150037eb82aeaf5bc7194d2682/fahrplan_mappings.json")
    suspend fun getFahrplanMappings(): Map<String, List<String>>
}