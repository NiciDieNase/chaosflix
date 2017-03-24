package de.nicidienase.chaosflix.network;

import java.util.List;

import de.nicidienase.chaosflix.entities.recording.Conference;
import de.nicidienase.chaosflix.entities.recording.Conferences;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.entities.recording.Recording;
import retrofit2.Retrofit;
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
				.build();
		service = retrofit.create(RecordingService.class);
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
	public Call<List<Event>> getEvents() {
		return service.getEvents();
	}

	@Override
	public Call<Event> getEvent(@Path("id") int id) {
		return service.getEvent(id);
	}

	@Override
	public Call<Recording> getRecording(@Path("id") int id) {
		return service.getRecording(id);
	}

	public void init(){
		try {
			long time = System.currentTimeMillis();
			List<Conference> conferences = this.listConferences().execute().body().getConferences();
			for(Conference conf : conferences){
				conf.setId((long) conf.getApiID());
				conf.save();
				for(Event e :conf.getEvents()){
					e.setParentConferenceID(conf.getId());
					e.setId((long) e.getApiID());
					e.save();
					for(Recording r : e.getRecordings()){
						r.setParentEventID(e.getId());
						r.setId((long) r.getApiID());
						r.save();
					}
				}
			}
			Log.d(TAG,String.format("updated entities in $1%i ms", System.currentTimeMillis() - time));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void update(){
		try {
			long time = System.currentTimeMillis();
			List<Conference> conferences = this.listConferences().execute().body().getConferences();
			for(Conference conf : conferences){
				Conference conferenceDB = Conference.findById(Conference.class, conf.getApiID());
				if(conferenceDB == null){
					conf.setId((long) conf.getApiID());
					conf.save();
				} else {
					conferenceDB.update(conf);
				}
				List<Event> eventList = this.getConference(conf.getApiID()).execute().body().getEvents();
				for(Event e :eventList){
					Event eventDB = Event.findById(Event.class, e.getApiID());
					if(eventDB == null){
						eventDB.setParentConferenceID(conf.getId());
						eventDB.setId((long) eventDB.getApiID());
						eventDB.save();
					} else {
						eventDB.update(e);
					}
					List<Recording> recordingList = this.getEvent(e.getApiID()).execute().body().getRecordings();
					for(Recording r : recordingList){
						Recording recordingDB = Recording.findById(Recording.class, r.getApiID());
						if(recordingDB == null){
							r.setParentEventID(eventDB.getId());
							r.setId((long) r.getApiID());
							r.save();
						} else {
							recordingDB.update(r);
						}
					}
				}
			}
			Log.d(TAG,String.format("updated entities in $1%i ms", System.currentTimeMillis() - time));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
