package de.nicidienase.chaosflix;


import com.google.common.collect.Lists;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import de.nicidienase.chaosflix.entities.recording.Conference;
import de.nicidienase.chaosflix.entities.recording.Event;
import de.nicidienase.chaosflix.network.RecordingClient;

/**
 * Created by felix on 17.03.17.
 */

public class RecordingClientTest {

	private static final String TAG = RecordingClientTest.class.getSimpleName();
	private RecordingClient client;

	@Before
	public void beforeTest() {
		this.client = new RecordingClient();
	}

	@Test
	public void getConferenceTest() throws IOException {
		client.getConference(101).subscribe
				(conference -> assertEquals("33c3", conference.getAcronym()));
	}

	@Test
	public void getEventTest() throws IOException {
		client.getEvent(3674).blockingSubscribe(
				event -> assertEquals("bfc2ab1f-8384-4d7d-801a-dde8c81e039c", event.getGuid()));
	}

	@Test
	public void getEventRecordingsTest() throws IOException {
		client.getEvent(3674).blockingSubscribe(event -> assertEquals(9, event.getRecordings().size()));
	}


	@Test
	public void getRecordingTest() throws IOException {
		client.getRecording(14142)
				.blockingSubscribe(recording -> assertEquals("2016-12-29T03:16:16.105+01:00",
						recording.getUpdatedAt()));
	}

	@Test
	public void getConferencEventListTest() throws IOException {
		client.getConferences().blockingSubscribe(conferences ->
				assertEquals(99, conferences.getConferences().size()));
	}

	@Test
	public void eventTagsTest() throws IOException {
		client.getConference(101).blockingSubscribe(
				conference -> assertEquals(12, conference.getEventsByTags().keySet().size()));
	}

	@Test
	public void sortTest() throws IOException {
		client.getConferences().blockingSubscribe(conferences -> {
			Collections.sort(conferences.getConferences());
			for (Conference conf : conferences.getConferences()) {
				client.getConference(conf.getApiID())
						.blockingSubscribe(conference -> Collections.sort(conference.getEvents()));
			}
		});
	}

	@Test
	public void sortAllEvents() throws IOException {
		client.getAllEvents().blockingSubscribe(events -> Collections.sort(events));
	}

	@Test
	public void mrmcd13() throws IOException {
		client.getConference(38).blockingSubscribe(conference ->
				Collections.sort(Lists.newArrayList(conference.getEventsByTags().keySet())));
	}

	@Test
	public void testTagsToTalksRation() throws IOException {
		client.getConferences().blockingSubscribe(conferences -> {
			for (Conference conf : conferences.getConferences()) {
				client.getConference(conf.getApiID()).blockingSubscribe(conference -> {
					System.out.print(conference.getAcronym() + ": " + conference.getEventsByTags().keySet());
					float sum = 0;
					for (Event e : conference.getEvents()) {
						sum += e.getTags().size();
					}
				});
			}
		});
	}
}
