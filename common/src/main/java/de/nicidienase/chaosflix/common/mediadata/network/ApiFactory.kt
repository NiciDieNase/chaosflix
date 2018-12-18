package de.nicidienase.chaosflix.common.mediadata.network

import android.content.res.Resources
import com.google.gson.Gson
import de.nicidienase.chaosflix.BuildConfig
import de.nicidienase.chaosflix.R
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiFactory(val res: Resources) {

    val gsonConverterFactory by lazy { GsonConverterFactory.create(Gson()) }

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
    }

    val recordingApi: RecordingService by lazy {
        Retrofit.Builder()
                .baseUrl(res.getString(R.string.recording_url))
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(RecordingService::class.java)
    }

    val streamingApi: StreamingService by lazy { Retrofit.Builder()
            .baseUrl(BuildConfig.STREAMING_API_BASE_URL)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(StreamingService::class.java) }
}