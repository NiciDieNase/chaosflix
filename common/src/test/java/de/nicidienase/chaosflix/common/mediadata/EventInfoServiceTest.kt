package de.nicidienase.chaosflix.common.mediadata

import de.nicidienase.chaosflix.common.mediadata.entities.eventinfo.EventInfo
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

class EventInfoServiceTest {

    private val apiFactory = ApiFactory.getInstance("https://api.media.ccc.de","https://c3voc.de", null)
    private val api = apiFactory.eventInfoApi

    @BeforeEach
    fun setup() {
    }

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


}
