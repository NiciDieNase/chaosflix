package de.nicidienase.chaosflix.shared.network;

import java.util.List;

import de.nicidienase.chaosflix.shared.entities.recording.Conference;
import de.nicidienase.chaosflix.shared.entities.recording.ConferencesWrapper;
import de.nicidienase.chaosflix.shared.entities.recording.Event;
import de.nicidienase.chaosflix.shared.entities.recording.Recording;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by felix on 17.03.17.
 */

public interface RecordingService {

	@GET("public/conferences")
	Observable<ConferencesWrapper> getConferences();

	@GET("public/conferences/{id}")
	Observable<Conference> getConference(@Path("id") long id);

	@GET("public/events")
	Observable<List<Event>> getAllEvents();

	@GET("public/events/{id}")
	Observable<Event> getEvent(@Path("id") long id);

	@GET("public/recordings/{id}")
	Observable<Recording> getRecording(@Path("id") long id);

}
