package de.nicidienase.chaosflix.touch;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.util.Log;

import java.util.List;

import de.nicidienase.chaosflix.common.entities.PlaybackProgress;
import de.nicidienase.chaosflix.common.entities.WatchlistItem;
import de.nicidienase.chaosflix.common.entities.recording.Conference;
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper;
import de.nicidienase.chaosflix.common.entities.recording.Event;
import de.nicidienase.chaosflix.common.entities.recording.Recording;
import de.nicidienase.chaosflix.common.entities.streaming.LiveConference;
import de.nicidienase.chaosflix.common.network.RecordingService;
import de.nicidienase.chaosflix.common.network.StreamingService;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by felix on 24.09.17.
 */

public class ChaosflixViewModel extends ViewModel {

	private static final String TAG = ChaosflixViewModel.class.getSimpleName();
	private final StreamingService mStreamingApi;
	private final RecordingService mRecordingApi;

	public ChaosflixViewModel(String recordingUrl, String streamingUrl){
		OkHttpClient client = new OkHttpClient();
		GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create();
		RxJava2CallAdapterFactory rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create();

		Retrofit retrofitRecordings = new Retrofit.Builder()
				.baseUrl(recordingUrl)
				.client(client)
				.addConverterFactory(gsonConverterFactory)
				.addCallAdapterFactory(rxJava2CallAdapterFactory)
				.build();
		mRecordingApi = retrofitRecordings.create(RecordingService.class);

		Retrofit retrofigStreaming = new Retrofit.Builder()
				.baseUrl(streamingUrl)
				.client(client)
				.addConverterFactory(gsonConverterFactory)
				.addCallAdapterFactory(rxJava2CallAdapterFactory)
				.build();
		mStreamingApi = retrofigStreaming.create(StreamingService.class);
	}

	public LiveData<ConferencesWrapper> getConferencesWrapperAsLiveData(){
		return new LiveData<ConferencesWrapper>() {
			@Override
			protected void onActive() {
				super.onActive();
				mRecordingApi.getConferences()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(conferencesWrapper -> setValue(conferencesWrapper));
			}
		};
	}
	public Observable<ConferencesWrapper> getConferencesWrapper() {
		return mRecordingApi.getConferences()
				.doOnError(throwable -> Log.d(TAG, String.valueOf(throwable.getCause())))
				.subscribeOn(Schedulers.io());
	}

	public Observable<List<Conference>> getConferencesByGroup(String group){
		return mRecordingApi.getConferences().map(
				conferencesWrapper -> conferencesWrapper.getConferencesBySeries().get(group))
				.subscribeOn(Schedulers.io());
	}

	public Observable<Conference> getConference(int mConferenceId) {
		return mRecordingApi.getConference(mConferenceId)
				.subscribeOn(Schedulers.io());
	}

	public Observable<Event> getEvent(int apiID) {
		return mRecordingApi.getEvent(apiID)
				.subscribeOn(Schedulers.io());
	}

	public Observable<Recording> getRecording(long id) {
		return mRecordingApi.getRecording(id)
				.subscribeOn(Schedulers.io());
	}

	public Observable<List<LiveConference>> getStreamingConferences() {
		return mStreamingApi.getStreamingConferences()
				.subscribeOn(Schedulers.io());
	}

	public void setPlaybackProgress(int apiId, long progress){
		getPlaybackProgress(apiId)
				.subscribe(p -> {
					if(p == 0l){
						PlaybackProgress playbackProgress = new PlaybackProgress(apiId, progress, 0);
						playbackProgress.setId((long) apiId);
						playbackProgress.save();
					} else {
						PlaybackProgress playbackProgress = PlaybackProgress.findById(PlaybackProgress.class, apiId);
						playbackProgress.setProgress(progress);
						playbackProgress.save();
					}
				});
	}

	public Observable<Long> getPlaybackProgress(int apiID) {
		return Observable.fromCallable(() -> {
			PlaybackProgress progress = PlaybackProgress.findById(PlaybackProgress.class, apiID);
			return progress != null ? progress.getProgress() : 0l;
		}).subscribeOn(Schedulers.io());
	}

	public void createBookmark(int apiId){
		WatchlistItem bookmark = getBookmark(apiId);
		if(bookmark != null){
			bookmark = new WatchlistItem(apiId);
			bookmark.save();
		} else {
			// Bookmark already exists
		}
	}

	public WatchlistItem getBookmark(int apiId){
		return WatchlistItem.findById(WatchlistItem.class, apiId);
	}

	public boolean bookmarkExists(int apiId) {
		return getBookmark(apiId) != null;
	}

	public boolean removeBookmark(int apiID) {
		return getBookmark(apiID).delete();
	}

	public static class Factory extends ViewModelProvider.NewInstanceFactory{

		private final String recordingUrl;
		private final String streamUrl;

		public Factory(String recordingUrl, String streamUrl){
			this.recordingUrl = recordingUrl;
			this.streamUrl = streamUrl;
		}

		@Override
		public <T extends ViewModel> T create(Class<T> modelClass) {
			return (T) new ChaosflixViewModel(recordingUrl,streamUrl);
		}
	}
}
