package de.nicidienase.chaosflix.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.gson.Gson
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentConference
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.network.RecordingService
import junit.framework.Assert.assertNotNull
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class IntegrationTests {

	lateinit var recordingApi: RecordingService
	@Before
	fun init(){

		val client = OkHttpClient.Builder()
				.connectTimeout(60, TimeUnit.SECONDS)
				.readTimeout(60, TimeUnit.SECONDS)
				.build()

		val rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()
		val gson = Gson()
		val gsonConverterFactory = GsonConverterFactory.create(gson)

		val retrofitRecordings = Retrofit.Builder()
				.baseUrl(recordingUrl)
				.client(client)
				.addConverterFactory(gsonConverterFactory)
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
		val conferencesWrapper = recordingApi.getConferencesWrapper().blockingGet()
		val conference = conferencesWrapper.conferences[0]
		val conferenceWithEvents = recordingApi.getConference(conference.conferenceID).blockingGet()
		val persistentConference = PersistentConference(conferenceWithEvents)
		val events = conferenceWithEvents.events?.map { PersistentEvent(it) }
		assertNotNull(persistentConference)
		assert(events?.size != 0)
	}

	@Test
	fun test2(){
		val emfCamp = recordingApi.getConference(91).blockingGet()
		assert("16:9".equals(emfCamp.aspectRatio))
	}

	companion object {
		val recordingUrl = "https://api.media.ccc.de"
	}
}