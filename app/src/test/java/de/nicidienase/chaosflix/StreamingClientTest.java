package de.nicidienase.chaosflix;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import de.nicidienase.chaosflix.entities.StreamingConference;
import de.nicidienase.chaosflix.network.StreamingClient;
import de.nicidienase.chaosflix.network.StreamingService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by felix on 23.03.17.
 */

public class StreamingClientTest {

	private static MockWebServer server;
	private static StreamingService service;


	@BeforeClass
	public static void setupClass() throws IOException {
		server = new MockWebServer();
		server.start();
		String serverUrl = server.url("/").toString();
		service = new StreamingClient(serverUrl);
	}

	@Before
	public void setupTest() throws IOException {
		server.enqueue(new MockResponse().setResponseCode(200).setBody(TestResources.testJson));
	}

	@Test
	public void test1() throws IOException {
		List<StreamingConference> streamingConferences
				= service.getStreamingConferences().execute().body();
		assertEquals(1,streamingConferences.size());
	}

	@Test
	public void test2() throws IOException {
		List<StreamingConference> streamingConferences
				= service.getStreamingConferences().execute().body();
		StreamingConference streamingConference = streamingConferences.get(0);
		assertEquals("FOSSGIS 2017",streamingConference.getConference());
	}
}
