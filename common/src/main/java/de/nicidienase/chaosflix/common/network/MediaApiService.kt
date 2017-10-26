package de.nicidienase.chaosflix.common.network

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log

import de.nicidienase.chaosflix.R
import de.nicidienase.chaosflix.common.entities.recording.Conference
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.entities.recording.Event
import de.nicidienase.chaosflix.common.entities.recording.Recording
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class MediaApiService (streamingUrl: String, recordingUrl: String) {
    private val TAG = MediaApiService::class.java.simpleName

    private var mRecordingApiService: RecordingService? = null
    private var mStreamingApiService: StreamingService? = null

    init {
        val client = OkHttpClient()
        val gsonConverterFactory = GsonConverterFactory.create()
        val rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

        val retrofitRecordings = Retrofit.Builder()
                .baseUrl(recordingUrl)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .build()
        mRecordingApiService = retrofitRecordings.create(RecordingService::class.java)

        val retrofigStreaming = Retrofit.Builder()
                .baseUrl(streamingUrl)
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .build()
        mStreamingApiService = retrofigStreaming.create(StreamingService::class.java)
    }

    val conferences: Observable<ConferencesWrapper>
        get() = mRecordingApiService!!.conferences
                .subscribeOn(Schedulers.io())

    val events: Observable<List<Event>>
        get() = mRecordingApiService!!.allEvents
                .subscribeOn(Schedulers.io())

    val streamingConferences: Observable<List<LiveConference>>
        get() = mStreamingApiService!!.streamingConferences
                .subscribeOn(Schedulers.io())

    fun getConference(id: Long): Observable<Conference> {
        return mRecordingApiService!!.getConference(id).subscribeOn(Schedulers.io())
    }

    fun getEvent(id: Long): Observable<Event> {
        return mRecordingApiService!!.getEvent(id).subscribeOn(Schedulers.io())
    }

    fun getRecording(id: Long): Observable<Recording> {
        return mRecordingApiService!!.getRecording(id).subscribeOn(Schedulers.io())
    }

    companion object {
        val RECORDING_URL = "recording_url"
        val STREAMING_URL = "streaming_url"
    }
}
