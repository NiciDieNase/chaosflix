package de.nicidienase.chaosflix;



import android.util.Log;

import com.google.common.collect.Lists;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.entities.Conferences;
import de.nicidienase.chaosflix.entities.Event;
import de.nicidienase.chaosflix.entities.Recording;
import de.nicidienase.chaosflix.network.MediaCCCClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by felix on 17.03.17.
 */

public class MediaCCCClientTest{

	private static final String TAG = MediaCCCClientTest.class.getSimpleName();
	private MediaCCCClient client;

	@Before
	public void beforeTest(){
		this.client = new MediaCCCClient();
	}

	@Test
	public void getConferenceTest() throws IOException {
		Conference conference = client.getConference(101).execute().body();
		assertEquals(conference.getAcronym(),"33c3");
	}

	@Test
	public void getEventTest() throws IOException {
		Event event = this.client.getEvent(3674).execute().body();
		assertEquals(event.getGuid(),"bfc2ab1f-8384-4d7d-801a-dde8c81e039c");
	}

	@Test
	public void getEventRecordingsTest() throws IOException {
		Event event = this.client.getEvent(3674).execute().body();
		assertEquals(event.getRecordings().size(),9);
	}


	@Test
	public void getRecordingTest() throws IOException {
		Recording recording = this.client.getRecording(14142).execute().body();
		assertEquals(recording.getUpdatedAt(), "2016-12-29T03:16:16.105+01:00");
	}

	@Test
	public void getConferencEventListTest() throws IOException {
		Conferences conferences = this.client.listConferences().execute().body();
		assertEquals(99,conferences.getConferences().size());
	}

	@Test
	public void eventTagsTest() throws IOException {
		Conference conference = this.client.getConference(101).execute().body();
		assertEquals(12,conference.getEventsByTags().keySet().size());
	}

	@Test
	public void sortTest() throws IOException {
		final MediaCCCClient client = this.client;
		Conferences conferences = client.listConferences().execute().body();
		Collections.sort(conferences.getConferences());
		for (Conference conf : conferences.getConferences()) {
			List<Event> events =
					client.getConference(conf.getApiID()).execute().body().getEvents();
			Collections.sort(events);
		}
	}

	@Test
	public void sortAllEvents() throws IOException {
		MediaCCCClient client = this.client;
		Collections.sort(client.getEvents().execute().body());
	}

	@Test
	public void mrmcd13() throws IOException {
		MediaCCCClient client = this.client;
		Conference conference = client.getConference(38).execute().body();
		ArrayList<String> keyList = Lists.newArrayList(conference.getEventsByTags().keySet());
		Collections.sort(keyList);
	}
}
