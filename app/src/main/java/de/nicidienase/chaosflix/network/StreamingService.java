package de.nicidienase.chaosflix.network;

import java.util.List;

import de.nicidienase.chaosflix.entities.streaming.LiveConference;
import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by felix on 23.03.17.
 */

public interface StreamingService {

	@GET("streams/v2.json")
	Observable<List<LiveConference>> getStreamingConferences();
}
