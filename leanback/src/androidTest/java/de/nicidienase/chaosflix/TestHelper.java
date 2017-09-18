package de.nicidienase.chaosflix;

import android.support.test.InstrumentationRegistry;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import okhttp3.mockwebserver.MockResponse;

/**
 * Created by felix on 25.03.17.
 */
public class TestHelper {

	static String getStringFromRaw(int resourceID) throws IOException {
		InputStream inputStream = InstrumentationRegistry
				.getContext().getResources().openRawResource(resourceID);
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer);
		return writer.toString();
	}

	static MockResponse getResponseForRaw(int resourceID) throws IOException {
		return new MockResponse().setBody(
				getStringFromRaw(resourceID));
	}
}
