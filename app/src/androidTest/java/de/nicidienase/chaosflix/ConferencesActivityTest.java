package de.nicidienase.chaosflix;


import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import de.nicidienase.chaosflix.test.R;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import de.nicidienase.chaosflix.leanback.activities.ConferencesActivity;
import okhttp3.mockwebserver.MockWebServer;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ConferencesActivityTest {

	@Rule
	public ActivityTestRule<ConferencesActivity> mActivityTestRule
			= new ActivityTestRule<>(ConferencesActivity.class, false, false);
	private static MockWebServer server;
	private UiDevice mDevice;


	@Before
	public void setup() throws IOException, TimeoutException {
		mDevice = UiDevice.getInstance(getInstrumentation());

		server = new MockWebServer();
		server.start();
		String serverUrl = server.url("").toString();

		server.enqueue(TestHelper.getResponseForRaw(R.raw.conferences_json));
		server.enqueue(TestHelper.getResponseForRaw(R.raw.conferences_101_33c3_json));

		Intent i = new Intent();
		i.putExtra("server_url",serverUrl);
		mActivityTestRule.launchActivity(i);
	}

	@Test
	public void conferencesActivityTest() throws InterruptedException {
		mDevice.pressDPadRight();
		mDevice.pressDPadRight();
		mDevice.pressDPadCenter();
	}

}
