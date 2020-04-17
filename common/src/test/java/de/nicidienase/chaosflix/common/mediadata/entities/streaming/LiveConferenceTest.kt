package de.nicidienase.chaosflix.common.mediadata.entities.streaming

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import java.util.Date

internal class LiveConferenceTest {

    @Test
    fun dateParseTest(){
        val parseDate = LiveConference.parseDate("2020-04-17T16:00:00+0000")
        val expected = Date(1587139200 * 1000L)
        assertThat(parseDate, equalTo<Date>(expected))
    }

    @Test
    fun dateParseTest2(){
        val parseDate = LiveConference.parseDate("2020-04-17T16:00:00+0000")
        val expected = Date(1587139200 * 1000L)
        assertThat(parseDate, equalTo<Date>(expected))
    }
}