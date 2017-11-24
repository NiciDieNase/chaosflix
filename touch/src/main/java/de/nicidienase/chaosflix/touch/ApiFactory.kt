package de.nicidienase.chaosflix.touch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

object ApiFactory {

    val recordingApi: RecordingService
    val streamingApi: StreamingService

    init {

        val res = ChaosflixApplication.APPLICATION_CONTEXT.resources
        val recordingUrl = res.getString(R.string.api_media_ccc_url)
        val streamingUrl = res.getString(R.string.streaming_media_ccc_url)

        val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
        val jacksonConverterFactory = JacksonConverterFactory.create(ObjectMapper().registerModule(KotlinModule()))
        val rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

        val retrofitRecordings = Retrofit.Builder()
                .baseUrl(recordingUrl)
                .client(client)
                .addConverterFactory(jacksonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .build()
        recordingApi = retrofitRecordings.create(RecordingService::class.java)

        val retrofigStreaming = Retrofit.Builder()
                .baseUrl(streamingUrl)
                .client(client)
                .addConverterFactory(jacksonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .build()
        streamingApi = retrofigStreaming.create(StreamingService::class.java)

    }
}