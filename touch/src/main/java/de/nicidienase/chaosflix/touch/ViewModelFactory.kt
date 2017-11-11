package de.nicidienase.chaosflix.touch

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.persistence.room.Room
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.nicidienase.chaosflix.BuildConfig
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.viewmodels.PlayerViewModel
import de.nicidienase.chaosflix.touch.viewmodels.BrowseViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

object ViewModelFactory: ViewModelProvider.Factory{

    val database: ChaosflixDatabase
    val recordingApi: RecordingService
    val streamingApi: StreamingService

    init {
        val res = ChaosflixApplication.APPLICATION_CONTEXT.resources
        val recordingUrl = res.getString(R.string.api_media_ccc_url)
        val streamingUrl = res.getString(R.string.streaming_media_ccc_url)

        val client = OkHttpClient.Builder()
                    .connectTimeout(60,TimeUnit.SECONDS)
                    .readTimeout(60,TimeUnit.SECONDS)
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

        database = Room.databaseBuilder(ChaosflixApplication.APPLICATION_CONTEXT,
                ChaosflixDatabase::class.java,"mediaccc.de").build()
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(BrowseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BrowseViewModel(database, recordingApi, streamingApi) as T
        } else if(modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            return PlayerViewModel(database, recordingApi, streamingApi) as T
        } else {
            throw UnsupportedOperationException("The requested ViewModel is currently unsupported. " +
                    "Please make sure to implement are correct creation of it. " +
                    " Request: ${modelClass.getCanonicalName()}");
        }

    }

}