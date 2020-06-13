package de.nicidienase.chaosflix.common.mediadata

import de.nicidienase.chaosflix.common.AnalyticsWrapper
import de.nicidienase.chaosflix.common.ChaosflixDatabase
import de.nicidienase.chaosflix.common.InstantExecutorExtension
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferenceDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.network.RecordingApi
import de.nicidienase.chaosflix.common.util.LiveEvent
import de.nicidienase.chaosflix.common.util.SingleLiveEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import retrofit2.Response

@ExtendWith(MockKExtension::class, InstantExecutorExtension::class)
internal class MediaRepositoryTest {

    @RelaxedMockK
    private lateinit var recordingApi: RecordingApi

    @RelaxedMockK
    private lateinit var analyticsWrapper: AnalyticsWrapper

    private var database: ChaosflixDatabase = mockk {
        every { conferenceGroupDao() } returns mockk(relaxed = true)
        every { conferenceDao() } returns mockk(relaxed = true)
        every { eventDao() } returns mockk(relaxed = true)
        every { relatedEventDao() } returns mockk(relaxed = true)
        every { recordingDao() } returns mockk(relaxed = true)
        every { playbackProgressDao() } returns mockk(relaxed = true)
        every { watchlistItemDao() } returns mockk(relaxed = true)
        every { offlineEventDao() } returns mockk(relaxed = true)
    }

    @RelaxedMockK
    private lateinit var updateState: SingleLiveEvent<LiveEvent<MediaRepository.State, List<Conference>, String>>

    private lateinit var mediaRepository: MediaRepository

    @BeforeEach
    fun setup() {
        mediaRepository = MediaRepository(recordingApi, database, analyticsWrapper)
    }

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }

    @Test
    fun testEmptyEventGetSuccess() = runBlocking {
        coEvery { recordingApi.getEventByGUIDSuspending(any()) } returns
                Response.error(404, ResponseBody.create(mediaTypeJson, emptyJson))
        mediaRepository.updateSingleEvent("foo")
        coVerify(exactly = 1) { recordingApi.getEventByGUIDSuspending("foo") }
        verify(exactly = 0) { analyticsWrapper.trackException(any()) }
    }

    @Test
    fun testIOException() = runBlocking {
        coEvery { recordingApi.getEventByGUIDSuspending(any()) } throws IOException("test")
        val result = mediaRepository.updateSingleEvent("foo")
        assertThat(null, equalTo(result))
        coVerify(exactly = 1) { recordingApi.getEventByGUIDSuspending("foo") }
        verify(exactly = 1) { analyticsWrapper.trackException(any()) }
    }

    @Test
    fun testUpdateConferencesAndGroupsSuccess() = runBlocking {
        coEvery { recordingApi.getConferencesWrapperSuspending() } returns Response.success(ConferencesWrapper(listOf(ConferenceDto(slug = "congress/2023"))))
        val slot = slot<LiveEvent<MediaRepository.State, List<Conference>, String>>()
        every { updateState.postValue(capture(slot)) } answers { println(slot.captured) }
        mediaRepository.apiOperations.updateConferencesAndGroupsInternal(updateState)
        val params = mutableListOf<LiveEvent<MediaRepository.State, List<Conference>, String>>()
        verify(exactly = 2) {
            updateState.postValue(withArg { params.add(it) })
        }
        assertThat(params[0].state, equalTo(MediaRepository.State.RUNNING))
        assertThat(params[0].data, nullValue())
        assertThat(params[1].state, equalTo(MediaRepository.State.DONE))
        assertThat(params[1].error, nullValue())
    }

    @Test
    fun testUpdateConferencesAndGroups500() = runBlocking {
        coEvery { recordingApi.getConferencesWrapperSuspending() } returns Response.error(500, ResponseBody.create(mediaTypeJson, "{}"))
        val slot = slot<LiveEvent<MediaRepository.State, List<Conference>, String>>()
//        every { updateState.postValue(capture(slot)) } answers {println(slot.captured)}
        mediaRepository.apiOperations.updateConferencesAndGroupsInternal(updateState)
        val params = mutableListOf<LiveEvent<MediaRepository.State, List<Conference>, String>>()
        verify(exactly = 2) {
            updateState.postValue(withArg { params.add(it) })
        }
        assertThat(params[0].state, equalTo(MediaRepository.State.RUNNING))
        assertThat(params[0].data, nullValue())
        assertThat(params[1].state, equalTo(MediaRepository.State.DONE))
        assertThat(params[1].data, nullValue())
        assertThat(params[1].error, notNullValue())
    }

    companion object {
        private val mediaTypeJson = MediaType.get("application/json")
        private const val emptyJson = "{}"
    }
}
