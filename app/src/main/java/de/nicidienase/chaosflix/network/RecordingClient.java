package de.nicidienase.chaosflix.network;

import java.util.List;

import de.nicidienase.chaosflix.entities.recording.Conference;
import de.nicidienase.chaosflix.entities.recording.ConferencesWrapper;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;
import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Path;

/**
 * Created by felix on 17.03.17.
 */

public class RecordingClient implements RecordingService {

	private static final String TAG = RecordingClient.class.getSimpleName();
	private final RecordingService service;

	public RecordingClient(){
		this("https://api.media.ccc.de");
	}

	public RecordingClient(String serviceUrl) {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(serviceUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.build();
		service = retrofit.create(RecordingService.class);
	}


	@Override
	public Observable<ConferencesWrapper> getConferences() {
		return service.getConferences();
	}

	@Override
	public Observable<Conference> getConference(@Path("id") long id) {
		return service.getConference(id);
	}

	@Override
	public Observable<List<Event>> getAllEvents() {
		return service.getAllEvents();
	}

	@Override
	public Observable<Event> getEvent(@Path("id") long id) {
		return service.getEvent(id);
	}

	@Override
	public Observable<Recording> getRecording(@Path("id") long id) {
		return service.getRecording(id);
	}
}
