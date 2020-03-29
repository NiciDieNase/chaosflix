package de.nicidienase.chaosflix.common.mediadata.network

import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RecordingServiceTest {

	private val apiFactory = ApiFactory.getInstance("https://api.media.ccc.de", null)

	@BeforeEach
	fun setup() {
	}

	@Test
	fun search() = runBlocking {
		val searchEvents = apiFactory.recordingApi.searchEvents("Bahn API Chaos - jetzt international", 1)
		assertThat(searchEvents.events.size, greaterThanOrEqualTo(1))


		val searchEvents2 = apiFactory.recordingApi.searchEvents("Bahn API Chaos - jetzt international", 2)
		assertThat(searchEvents2.events.size, equalTo(0))
	}

	@Test
	fun bahnApiChaos() = runBlocking {
		val searchEvents = apiFactory.recordingApi.searchEvents("Bahn API Chaos", 1)
		assertThat(searchEvents.events.size, greaterThanOrEqualTo(4))
	}
}