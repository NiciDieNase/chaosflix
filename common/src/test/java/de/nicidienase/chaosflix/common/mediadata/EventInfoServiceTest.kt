package de.nicidienase.chaosflix.common.mediadata

import de.nicidienase.chaosflix.StageConfiguration
import de.nicidienase.chaosflix.common.TestStageConfig
import de.nicidienase.chaosflix.common.mediadata.entities.eventinfo.EventInfo
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class EventInfoServiceTest {

    private val apiFactory = ApiFactory.getInstance(TestStageConfig)
    private val api = apiFactory.eventInfoApi

    @Test
    fun checkRequestWorks() = runBlocking {
        val vocEvents = api.getVocEvents()
        assertThat(vocEvents.events.values.size, Matchers.greaterThanOrEqualTo(300))
    }

    @Test
    fun checkConversion() = runBlocking {
        val vocEvents = api.getVocEvents()
        val eventInfos = vocEvents.events.values.mapNotNull { EventInfo.fromVocEventDto(it) }
        val now = Date()
        assertThat(eventInfos.size, Matchers.equalTo(vocEvents.events.values.size))
        val partition = eventInfos.partition { it.startDate.after(now) }
        assertThat(partition.first.size, Matchers.lessThanOrEqualTo(partition.second.size))
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
