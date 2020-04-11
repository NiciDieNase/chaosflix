package de.nicidienase.chaosflix.common.mediadata

import androidx.lifecycle.MutableLiveData
import de.nicidienase.chaosflix.common.AnalyticsWrapperImpl
import de.nicidienase.chaosflix.common.InstantExecutorExtension
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.LiveConference
import de.nicidienase.chaosflix.common.mediadata.network.StreamingApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
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
import javax.net.ssl.SSLHandshakeException

@ExtendWith(MockKExtension::class, InstantExecutorExtension::class)
internal class StreamingRepositoryTest {

    @RelaxedMockK
    private lateinit var streamingApi: StreamingApi

    @RelaxedMockK
    private lateinit var _streamingConferences: MutableLiveData<List<LiveConference>>

    @InjectMockKs
    private lateinit var streamingRepository: StreamingRepository

    @BeforeEach
    fun setup(){
        mockkObject(AnalyticsWrapperImpl)
    }

    @AfterEach
    fun cleanup(){
        unmockkAll()
    }

    @Test
    fun handleSuccess() = runBlocking {
        val testConferences = listOf(LiveConference("divoc", "test"))
        coEvery { streamingApi.getStreamingConferences() } returns Response.success(200, testConferences)
        streamingRepository.update()
        assertThat(testConferences,equalTo(streamingRepository.streamingConferences.value))
    }

    @Test
    fun handleSSLException() = runBlocking {
        coEvery { streamingApi.getStreamingConferences() } throws SSLHandshakeException("")
        streamingRepository.update()
        coVerify(exactly = 0) { _streamingConferences.postValue(any()) }
        verify(exactly = 1) { AnalyticsWrapperImpl.trackException(any()) }
    }

    @Test
    fun handle404() = runBlocking {
        coEvery { streamingApi.getStreamingConferences() } returns Response.error(404, ResponseBody.create(MediaType.get("text/plain"), "not found"))
        mockkObject(AnalyticsWrapperImpl)
        streamingRepository.update()
        coVerify(exactly = 0) { _streamingConferences.postValue(any()) }
        verify(exactly = 0) { AnalyticsWrapperImpl.trackException(any()) }
    }
}