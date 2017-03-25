package de.nicidienase.chaosflix;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by felix on 25.03.17.
 */
@RunWith(AndroidJUnit4.class)
public class MediaCccDispatcherTest {
	private static final String TAG = MediaCccDispatcher.class.getSimpleName();
	private MediaCccDispatcher dispatcher;

	@Before
	public void setUp() throws Exception {
		dispatcher = new MediaCccDispatcher();
	}

	@Test
	public void test200() throws IOException {
		String status = dispatcher.getResponseForPath("/public/conferences").getStatus();
		Log.d(TAG,status);
		assertTrue(status.contains("200"));
	}

	@Test
	public void testConferences101() throws IOException {
		String status = dispatcher.getResponseForPath("/public/conferences/101").getStatus();
		Log.d(TAG,status);
		assertTrue(status.contains("200"));
	}

	@Test
	public void test404() throws IOException {
		String status = dispatcher.getResponseForPath("/foo/bar").getStatus();
		Log.d(TAG,status);
		assertTrue(status.contains("404"));
	}

	@Test
	public void testEvents() throws IOException {
		String status = dispatcher.getResponseForPath("/public/events").getStatus();
		Log.d(TAG,status);
		assertTrue(status.contains("200"));
	}

	@Test
	public void testEvent3062() throws IOException {
		String status = dispatcher.getResponseForPath("/public/events/3062").getStatus();
		Log.d(TAG,status);
		assertTrue(status.contains("200"));
	}

	@Test
	public void testStreams() throws IOException {
		String status = dispatcher.getResponseForPath("/streams/v2.json").getStatus();
		Log.d(TAG,status);
		assertTrue(status.contains("200"));
	}

	@Test
	public void testStreams2() throws IOException {
		String status = dispatcher.getResponseForPath("/streams/foobar").getStatus();
		Log.d(TAG,status);
		assertTrue(status.contains("200"));
	}

}