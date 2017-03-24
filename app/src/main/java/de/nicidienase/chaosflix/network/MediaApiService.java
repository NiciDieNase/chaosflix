package de.nicidienase.chaosflix.network;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.entities.recording.Conference;
import de.nicidienase.chaosflix.entities.recording.Conferences;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;
import de.nicidienase.chaosflix.entities.streaming.LiveConference;
import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Path;

/**
 * Created by felix on 24.03.17.
 */

public class MediaApiService extends Service {

	private final IBinder mBinder = new LocalBinder();
	private final RecordingService mRecordingApiService;
	private final StreamingService mStreamingApiService;

	public class LocalBinder extends Binder {
		public MediaApiService getService() {
			return MediaApiService.this;
		}
	}

	public MediaApiService(){
		OkHttpClient client = new OkHttpClient();
		GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create();
		RxJava2CallAdapterFactory rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create();

		Retrofit retrofitRecordings = new Retrofit.Builder()
				.baseUrl(getString(R.string.api_media_ccc_url))
				.client(client)
				.addConverterFactory(gsonConverterFactory)
				.addCallAdapterFactory(rxJava2CallAdapterFactory)
				.build();
		mRecordingApiService = retrofitRecordings.create(RecordingService.class);

		Retrofit retrofigStreaming = new Retrofit.Builder()
				.baseUrl(getString(R.string.streaming_media_ccc_url))
				.client(client)
				.addConverterFactory(gsonConverterFactory)
				.addCallAdapterFactory(rxJava2CallAdapterFactory)
				.build();
		mStreamingApiService = retrofigStreaming.create(StreamingService.class);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public Observable<Conferences> getConferences(){
		return mRecordingApiService.getConferences();
	};

	public Observable<Conference> getConference(long id){
		return mRecordingApiService.getConference(id);
	}

	public Observable<List<Event>> getEvents(){
		return mRecordingApiService.getAllEvents();
	}

	public Observable<Event> getEvent(long id){
		return mRecordingApiService.getEvent(id);
	}

	public Observable<Recording> getRecording(long id){
		return mRecordingApiService.getRecording(id);
	}

	public Observable<List<LiveConference>> getStreamingConferences(){
		return mStreamingApiService.getStreamingConferences();
	}
}
