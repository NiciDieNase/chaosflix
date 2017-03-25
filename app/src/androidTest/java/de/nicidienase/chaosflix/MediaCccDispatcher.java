package de.nicidienase.chaosflix;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Created by felix on 25.03.17.
 */
public class MediaCccDispatcher extends Dispatcher {

	private static final String TAG = MediaCccDispatcher.class.getSimpleName();

	@Override
	public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
		try {
			String path = request.getPath();
			return getResponseForPath(path);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return new MockResponse().setResponseCode(404);
		}
	}

	@NonNull
	protected MockResponse getResponseForPath(String path) throws IOException {
		String[] split = path.split("/");
		Log.d(TAG, path);
		switch (split[1]) {
			case "public":
				switch (split[2]) {
					case "conferences":
						if (split.length <= 3) {
							return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.conferences_json));
						} else {
							switch (split[3]) {
								case "101":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.conferences_101_33c3_json));
								case "13":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.conferences_13_mrmcd101b_json));
								case "21":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.conferences_21_18c3_json));
								case "78":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.conferences_78_32c3_json));
								case "85":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.conferences_85_gpn16_json));

							}

						}
					case "events":
						if (split.length <= 3) {
							return new MockResponse()
									.setResponseCode(200)
									.setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.events_json));
						} else {
							switch (split[3]) {
								case "2837":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.events_2837_json));
								case "2848":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.events_2848_json));
								case "3062":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.events_3062_json));
								case "3066":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.events_3066_json));
								case "3104":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.events_3104_json));
								case "3623":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.events_3623_json));
								case "3768":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.events_3768_json));
								case "3668":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.events_3668_json));
								case "709":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.events_709_json));
								case "710":
									return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.events_710_json));
							}
						}
				}
			case "streams":
				return new MockResponse().setBody(getStringFromRaw(de.nicidienase.chaosflix.test.R.raw.streams_v2_json));
			default:
				return new MockResponse().setResponseCode(404);
		}
	}

	protected static String getStringFromRaw(int resourceID) throws IOException {
		InputStream inputStream = InstrumentationRegistry
				.getContext().getResources().openRawResource(resourceID);
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer);
		return writer.toString();
	}
}
