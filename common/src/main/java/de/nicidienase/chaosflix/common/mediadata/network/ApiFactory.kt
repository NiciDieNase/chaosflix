package de.nicidienase.chaosflix.common.mediadata.network

import android.os.Build
import android.util.Log
import com.google.gson.Gson
import de.nicidienase.chaosflix.BuildConfig
import de.nicidienase.chaosflix.common.SingletonHolder2
import java.io.File
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiFactory private constructor(private val recordingUrl: String, cache: File? = null) {

    private val chaosflixUserAgent: String by lazy { buildUserAgent() }
    private val gsonConverterFactory: GsonConverterFactory by lazy { GsonConverterFactory.create(Gson()) }

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .cache(Cache(cache, CACHE_SIZE))
            .addInterceptor(useragentInterceptor)
            .build()
    }

    val recordingApi: RecordingService by lazy {
        Retrofit.Builder()
            .baseUrl(recordingUrl)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(RecordingService::class.java)
    }

    val streamingApi: StreamingApi by lazy { Retrofit.Builder()
        .baseUrl(BuildConfig.STREAMING_API_BASE_URL)
        .client(client)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(StreamingApi::class.java) }

    private val useragentInterceptor: Interceptor = Interceptor { chain ->
        val requestWithUseragent = chain.request().newBuilder()
            .header("User-Agent", chaosflixUserAgent)
            .build()
        try {
            return@Interceptor chain.proceed(requestWithUseragent)
        } catch (ex: SocketTimeoutException) {
            Log.e("UserAgentIntercepor", ex.message, ex)
            return@Interceptor null
        }
    }

    companion object : SingletonHolder2<ApiFactory, String, File?>(::ApiFactory) {

        private const val DEFAULT_TIMEOUT = 30L
        private const val CACHE_SIZE = 1024L * 5 // 5MB

        fun buildUserAgent(): String {
            val versionName = BuildConfig.VERSION_NAME
            val device = "${Build.BRAND} ${Build.MODEL}"
            val osVersion = "Android/${Build.VERSION.RELEASE}"
            return "chaosflix/$versionName $osVersion ($device)"
        }
    }
}
