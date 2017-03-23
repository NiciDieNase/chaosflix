package de.nicidienase.chaosflix.network;

import java.util.List;

import de.nicidienase.chaosflix.entities.StreamingConference;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by felix on 23.03.17.
 */

public class StreamingClient implements StreamingService {

	private StreamingService service;

	public StreamingClient(){
		this("https://api.media.ccc.de");
	}

	public StreamingClient(String serviceUrl){
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(serviceUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		service = retrofit.create(StreamingService.class);
	}

	@Override
	public Call<List<StreamingConference>> getStreamingConferences() {
		return service.getStreamingConferences();
	}
}
