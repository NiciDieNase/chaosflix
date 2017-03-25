package de.nicidienase.chaosflix;


import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import de.nicidienase.chaosflix.test.R;
import de.nicidienase.chaosflix.network.MediaApiService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * Created by felix on 17.03.17.
 */
@RunWith(AndroidJUnit4.class)
public class MediaApiServiceTest {

	@ClassRule
	public static final ServiceTestRule mServiceRule = new ServiceTestRule();

	private static final String TAG = MediaApiServiceTest.class.getSimpleName();
	private static MockWebServer server;
	private static String serverUrl;
	private static MediaApiService service;


	@BeforeClass
	public static void setup() throws IOException, TimeoutException {
		server = new MockWebServer();
		server.start();
		serverUrl = server.url("").toString();

		Intent s = new Intent(InstrumentationRegistry.getTargetContext(),
				MediaApiService.class);
		s.putExtra(MediaApiService.RECORDING_URL,serverUrl);
		s.putExtra(MediaApiService.STREAMING_URL,serverUrl);
		IBinder binder = mServiceRule.bindService(s);
		service = ((MediaApiService.LocalBinder) binder).getService();
	}

	@After
	public void cleanup(){
	}


	@Test
	public void getConferenceTest() throws IOException {
		server.enqueue(TestHelper.getResponseForRaw(R.raw.conferences_101_33c3_json));

		service.getConference(101)
				.doOnError(throwable -> fail())
				.blockingSubscribe
				(conference -> assertThat(conference.getAcronym(),is("33c3")));
	}

	@Test
	public void sortAllEvents() throws IOException {
		server.enqueue(TestHelper.getResponseForRaw(R.raw.events_json));

		service.getEvents()
				.doOnError(throwable -> fail())
				.blockingSubscribe(events -> Collections.sort(events));
	}


	@Test
	public void getEventTest() throws IOException {
		server.enqueue(TestHelper.getResponseForRaw(R.raw.events_2837_json));

		service.getEvent(2837)
				.doOnError(throwable -> fail())
				.blockingSubscribe(
				event -> assertThat(event.getGuid(), is("9f2e9ff0-1555-470b-8743-9f07f54e9097")));
	}

	@Test
	public void test1() throws IOException {
		server.enqueue(TestHelper.getResponseForRaw(R.raw.streams_v2_json));

		service.getStreamingConferences()
				.doOnError(throwable -> fail())
				.blockingSubscribe(liveConferences -> assertThat(1,is(liveConferences.size())));

	}

	@Test
	public void test2() throws IOException {
		server.enqueue(TestHelper.getResponseForRaw(R.raw.streams_v2_json));

		service.getStreamingConferences()
				.doOnError(throwable -> fail())
				.blockingSubscribe(liveConferences -> assertThat("FOSSGIS 2017",is(liveConferences.get(0).getConference())));
	}
}
