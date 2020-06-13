package de.nicidienase.chaosflix.common.mediadata.network

import android.os.Build
import com.google.gson.Gson
import de.nicidienase.chaosflix.StageConfiguration
import de.nicidienase.chaosflix.common.SingletonHolder
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.core.KoinComponent
import org.koin.core.get
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiFactory(private val stageConfiguration: StageConfiguration) {

    private val chaosflixUserAgent: String by lazy { buildUserAgent() }
    private val gsonConverterFactory: GsonConverterFactory by lazy { GsonConverterFactory.create(Gson()) }

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(useragentInterceptor)
                .apply {
                    if (stageConfiguration.cacheDir != null) {
                        cache(Cache(stageConfiguration.cacheDir, CACHE_SIZE))
                    }
                }
                .build()
    }

    val recordingApi: RecordingApi by lazy {
        Retrofit.Builder()
                .baseUrl(stageConfiguration.recordingUrl)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(RecordingApi::class.java)
    }

    val streamingApi: StreamingApi by lazy {
        Retrofit.Builder()
                .baseUrl(stageConfiguration.streamingApiBaseUrl)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(StreamingApi::class.java)
    }

    val eventInfoApi: EventInfoApi by lazy {
        Retrofit.Builder()
                .baseUrl(stageConfiguration.eventInfoUrl)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(EventInfoApi::class.java)
    }

    private val useragentInterceptor: Interceptor = Interceptor { chain ->
        val requestWithUseragent = chain.request().newBuilder()
                .header("User-Agent", chaosflixUserAgent)
                .build()
        return@Interceptor chain.proceed(requestWithUseragent)
    }

    companion object : SingletonHolder<ApiFactory, StageConfiguration>(::ApiFactory), KoinComponent {

        private const val DEFAULT_TIMEOUT = 30L
        private const val CACHE_SIZE = 1024L * 5 // 5MB

        fun buildUserAgent(): String {
            val versionName = get<StageConfiguration>().versionName
            val device = "${Build.BRAND} ${Build.MODEL}"
            val osVersion = "Android/${Build.VERSION.RELEASE}"
            return "chaosflix/$versionName $osVersion ($device)"
        }
    }
}
