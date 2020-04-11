package de.nicidienase.chaosflix.common.mediadata

import de.nicidienase.chaosflix.common.AnalyticsWrapperImpl
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.InstantExecutorExtension
import de.nicidienase.chaosflix.common.mediadata.network.RecordingApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import retrofit2.Response
import java.io.IOException

@ExtendWith(MockKExtension::class, InstantExecutorExtension::class)
internal class MediaRepositoryTest {

    @RelaxedMockK
    private lateinit var recordingApi: RecordingApi

    private var database: ChaosflixDatabase = mockk {
        every {conferenceGroupDao() } returns mockk(relaxed = true)
        every {conferenceDao() } returns mockk(relaxed = true)
        every {eventDao() } returns mockk(relaxed = true)
        every {relatedEventDao() } returns mockk(relaxed = true)
        every {recordingDao() } returns mockk(relaxed = true)
        every {playbackProgressDao() } returns mockk(relaxed = true)
        every {watchlistItemDao() } returns mockk(relaxed = true)
        every {offlineEventDao() } returns mockk(relaxed = true)
    }

    @InjectMockKs
    private lateinit var mediaRepository: MediaRepository

    @BeforeEach
    fun setup(){
        mockkObject(AnalyticsWrapperImpl)
    }

    @AfterEach
    fun cleanup(){
        unmockkAll()
    }

    @Test
    fun testEmptyEventGetSuccess() = runBlocking {
        coEvery { recordingApi.getEventByGUIDSuspending(any()) } returns
                Response.error(404, ResponseBody.create(MediaType.get("application/json"),""))
        mediaRepository.updateSingleEvent("foo")
        coVerify(exactly = 1) { recordingApi.getEventByGUIDSuspending("foo") }
        verify(exactly = 0) { AnalyticsWrapperImpl.trackException(any()) }
    }

    @Test
    fun testIOException() = runBlocking {
        coEvery { recordingApi.getEventByGUIDSuspending(any()) } throws IOException("test")
        val result = mediaRepository.updateSingleEvent("foo")
        assertThat(null, equalTo(result))
        coVerify(exactly = 1) { recordingApi.getEventByGUIDSuspending("foo") }
        verify(exactly = 1) { AnalyticsWrapperImpl.trackException(any()) }
    }
}