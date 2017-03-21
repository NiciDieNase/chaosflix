package de.nicidienase.chaosflix;



import static org.junit.Assert.*;

import org.junit.Test;

import java.io.IOException;

import de.nicidienase.chaosflix.entities.Conference;
import de.nicidienase.chaosflix.entities.Conferences;
import de.nicidienase.chaosflix.entities.Event;
import de.nicidienase.chaosflix.entities.Recording;
import de.nicidienase.chaosflix.network.MediaCCCClient;

/**
 * Created by felix on 17.03.17.
 */

public class MediaCCCClientTest{

	@Test
	public void getConferenceTest(){
		try {
			Conference conference = new MediaCCCClient().getConference(101).execute().body();
			assertEquals(conference.getAcronym(),"33c3");
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getEventTest(){
		try {
			Event event = new MediaCCCClient().getEvent(3674).execute().body();
			assertEquals(event.getGuid(),"bfc2ab1f-8384-4d7d-801a-dde8c81e039c");
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getEventRecordingsTest(){
		try {
			Event event = new MediaCCCClient().getEvent(3674).execute().body();
			assertEquals(event.getRecordings().size(),9);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}


	@Test
	public void getRecordingTest(){
		try {
			Recording recording = new MediaCCCClient().getRecording(14142).execute().body();
			assertEquals(recording.getUpdatedAt(), "2016-12-29T03:16:16.105+01:00");
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getConferencEventListTest(){
		try {
			Conferences conferences = new MediaCCCClient().listConferences().execute().body();
			assertEquals(99,conferences.getConferences().size());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void eventTagsTest(){
		try {
			Conference conference = new MediaCCCClient().getConference(101).execute().body();
			assertEquals(12,conference.getEventsByTags().keySet().size());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
