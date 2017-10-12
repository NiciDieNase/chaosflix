package de.nicidienase.chaosflix.touch;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;
import android.content.res.Resources;
import android.util.Log;

import java.util.List;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase;
import de.nicidienase.chaosflix.common.entities.PlaybackProgress;
import de.nicidienase.chaosflix.common.entities.WatchlistItem;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.common.entities.recording.Recording;
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference;
import de.nicidienase.chaosflix.common.network.RecordingService;
import de.nicidienase.chaosflix.common.network.StreamingService;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by felix on 24.09.17.
 */

public class ChaosflixViewModel extends AndroidViewModel {

	private static final String TAG = ChaosflixViewModel.class.getSimpleName();

	private final StreamingService streamingApi;
	private final RecordingService recordingApi;
	private final ChaosflixDatabase database;
	CompositeDisposable disposable = new CompositeDisposable();

	public ChaosflixViewModel(Application application){
		super(application);
		Resources res = application.getResources();
		String recordingUrl = res.getString(R.string.api_media_ccc_url);
		String streamingUrl = res.getString(R.string.streaming_media_ccc_url);

		OkHttpClient client = new OkHttpClient();
		GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create();
		RxJava2CallAdapterFactory rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create();

		Retrofit retrofitRecordings = new Retrofit.Builder()
				.baseUrl(recordingUrl)
				.client(client)
				.addConverterFactory(gsonConverterFactory)
				.addCallAdapterFactory(rxJava2CallAdapterFactory)
				.build();
		recordingApi = retrofitRecordings.create(RecordingService.class);

		Retrofit retrofigStreaming = new Retrofit.Builder()
				.baseUrl(streamingUrl)
				.client(client)
				.addConverterFactory(gsonConverterFactory)
				.addCallAdapterFactory(rxJava2CallAdapterFactory)
				.build();
		streamingApi = retrofigStreaming.create(StreamingService.class);

		database = Room.databaseBuilder(getApplication().getApplicationContext(), ChaosflixDatabase.class, "mediaccc.db").build();
	}

	public LiveData<ConferencesWrapper> getConferencesWrapperAsLiveData(){
		return new LiveData<ConferencesWrapper>() {
			@Override
			protected void onActive() {
				super.onActive();
				recordingApi.getConferences()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(conferencesWrapper -> setValue(conferencesWrapper));
			}
		};
	}
	public Observable<ConferencesWrapper> getConferencesWrapper() {
		return recordingApi.getConferences()
				.doOnError(throwable -> Log.d(TAG, String.valueOf(throwable.getCause())))
				.subscribeOn(Schedulers.io());
	}

	public Observable<List<Conference>> getConferencesByGroup(String group){
		return recordingApi.getConferences().map(
				conferencesWrapper -> conferencesWrapper.getConferencesBySeries().get(group))
				.subscribeOn(Schedulers.io());
	}

	public Observable<Conference> getConference(int mConferenceId) {
		return recordingApi.getConference(mConferenceId)
				.subscribeOn(Schedulers.io());
	}

	public Observable<Event> getEvent(int apiID) {
		return recordingApi.getEvent(apiID)
				.subscribeOn(Schedulers.io());
	}

	public Observable<Recording> getRecording(long id) {
		return recordingApi.getRecording(id)
				.subscribeOn(Schedulers.io());
	}

	public Observable<List<LiveConference>> getStreamingConferences() {
		return streamingApi.getStreamingConferences()
				.subscribeOn(Schedulers.io());
	}

	public void setPlaybackProgress(int apiId, long progress){
		database.playbackProgressDao().getProgressForEvent(apiId)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.io())
				.subscribe(playbackProgress -> {
					if(playbackProgress != null){
						playbackProgress.setProgress(progress);
					} else {
						playbackProgress = new PlaybackProgress(apiId,progress);
					}
					database.playbackProgressDao().saveProgress(playbackProgress);
				});
	}

	public Flowable<PlaybackProgress> getPlaybackProgress(int apiID) {
		return database.playbackProgressDao().getProgressForEvent(apiID);
	}

	public void createBookmark(int apiId){
		database.watchlistItemDao().getItemForEvent(apiId)
				.subscribe(watchlistItem -> {
					if(watchlistItem == null){
						watchlistItem = new WatchlistItem(apiId,apiId);
						database.watchlistItemDao().saveItem(watchlistItem);
					}
				});
	}

	public Flowable<WatchlistItem> getBookmark(int apiId){
		return database.watchlistItemDao().getItemForEvent(apiId);
	}

	public void removeBookmark(int apiID) {
		getBookmark(apiID).subscribeOn(Schedulers.io())
				.observeOn(Schedulers.io())
				.subscribe(watchlistItem ->
			database.watchlistItemDao().deleteItem(watchlistItem));
	}
}
