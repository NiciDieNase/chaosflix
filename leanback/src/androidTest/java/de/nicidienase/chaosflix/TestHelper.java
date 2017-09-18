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
