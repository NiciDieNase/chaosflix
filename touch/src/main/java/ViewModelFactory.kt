package de.nicidienase.chaosflix.touch

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.persistence.room.Room
import de.nicidienase.chaosflix.ChaosflixApplication
import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import de.nicidienase.chaosflix.touch.BrowseViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ViewModelFactory: ViewModelProvider.Factory{

    val database: ChaosflixDatabase
    val recordingApi: RecordingService
    val streamingApi: StreamingService

    init {
        val res = ChaosflixApplication.getContext().getResources()
        val recordingUrl = res.getString(R.string.api_media_ccc_url)
        val streamingUrl = res.getString(R.string.streaming_media_ccc_url)

        val client = OkHttpClient()
        val gsonConverterFactory = GsonConverterFactory.create()
        val rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

        val retrofitRecordings = Retrofit.Builder()
                .baseUrl(recordingUrl)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .build()
        recordingApi = retrofitRecordings.create(RecordingService::class.java)

        val retrofigStreaming = Retrofit.Builder()
                .baseUrl(streamingUrl)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .build()
        streamingApi = retrofigStreaming.create(StreamingService::class.java)

        database = Room.databaseBuilder(ChaosflixApplication.getContext(),
                ChaosflixDatabase::class.java,"mediaccc.de").build()
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(BrowseViewModel::class.java)){
            return BrowseViewModel(database, recordingApi, streamingApi) as T
        } else {
            throw UnsupportedOperationException("The requested ViewModel is currently unsupported. " +
                    "Please make sure to implement are correct creation of it. " +
                    " Request: ${modelClass.getCanonicalName()}");
        }

    }

}