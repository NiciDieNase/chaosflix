package de.nicidienase.chaosflix.network;

import java.util.List;

import de.nicidienase.chaosflix.entities.StreamingConference;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by felix on 23.03.17.
 */

public interface StreamingService {

	@GET("streams/v2.json")
	Call<List<StreamingConference>> getStreamingConferences();
}
