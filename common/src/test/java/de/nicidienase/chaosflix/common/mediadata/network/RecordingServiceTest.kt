package de.nicidienase.chaosflix.common.mediadata.network

import de.nicidienase.chaosflix.StageConfiguration
import de.nicidienase.chaosflix.common.TestStageConfig
import de.nicidienase.chaosflix.common.mediadata.MediaRepository
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class RecordingServiceTest {

    private val apiFactory = ApiFactory.getInstance(TestStageConfig)
    private val api = apiFactory.recordingApi

    @Test
    fun search() = runBlocking {
        val searchEvents = api.searchEvents("Bahn API Chaos - jetzt international", 1).body()!!
        assertThat(searchEvents.events.size, greaterThanOrEqualTo(1))

        val searchEvents2 = api.searchEvents("Bahn API Chaos - jetzt international", 2).body()!!
        assertThat(searchEvents2.events.size, equalTo(0))
    }

    @Test
    fun bahnApiChaos() = runBlocking {
        val searchEvents = api.searchEvents("Bahn API Chaos", 1).body()!!
        assertThat(searchEvents.events.size, greaterThanOrEqualTo(4))
    }

    @Test
    fun git() = runBlocking {
        val response = api.searchEvents("git")
        val response2 = api.searchEvents("git", 2)
        val result = listOf(response, response2).map {
            it.headers().let {
                it.get("total") to MediaRepository.parseLink(it.get("link") ?: "")
            }
        }
        println(result)
    }

    companion object {

        @BeforeAll
        @JvmStatic
        fun setup() {
            startKoin {
                modules(
                        module {
                            single<StageConfiguration> { TestStageConfig }
                        }
                )
            }
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            stopKoin()
        }
    }
}
