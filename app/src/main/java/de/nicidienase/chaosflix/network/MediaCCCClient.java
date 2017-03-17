package de.nicidienase.chaosflix.network;

import java.util.List;

import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.entities.Conferences;
import de.nicidienase.chaosflix.entities.Event;
import de.nicidienase.chaosflix.entities.Recording;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Path;

/**
 * Created by felix on 17.03.17.
 */

public class MediaCCCClient implements  MediaCCCService{
	private final MediaCCCService service;

	public MediaCCCClient() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://api.media.ccc.de")
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		service = retrofit.create(MediaCCCService.class);
	}


	@Override
	public Call<Conferences> listConferences() {
		return service.listConferences();
	}

	@Override
	public Call<Conference> getConference(@Path("id") int id) {
		return service.getConference(id);
	}

	@Override
	public Call<Event> getEvent(@Path("id") int id) {
		return service.getEvent(id);
	}

	@Override
	public Call<Recording> getRecording(@Path("id") int id) {
		return service.getRecording(id);
	}
}
