package de.nicidienase.chaosflix.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.nicidienase.chaosflix.common.network.RecordingService
import de.nicidienase.chaosflix.common.network.StreamingService
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit


class IntegrationTests {

	lateinit var recordingApi: RecordingService
	@Before
	fun init(){

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


//		val retrofigStreaming = Retrofit.Builder()
//				.baseUrl(streamingUrl)
//				.client(client)
//				.addConverterFactory(jacksonConverterFactory)
//				.addCallAdapterFactory(rxJava2CallAdapterFactory)
//				.build()
//		streamingApi = retrofigStreaming.create(StreamingService::class.java)
	}

	@Test
	fun test1(){
		val allEvents = recordingApi.getAllEvents()

	}


	companion object {
		val recordingUrl = "https://api.media.ccc.de"
	}
}