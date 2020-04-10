package de.nicidienase.chaosflix.common

import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ChaosflixUtilTest {

    val api = ApiFactory.getInstance("https://api.media.ccc.de", null).recordingApi

    @Test
    fun testGPN19() = genericTest("gpn19", false)

    @Test
    fun testGPN18() = genericTest("gpn18", false)

    @Test
    fun testGPN17() = genericTest("gpn17", false)

    @Test
    fun testGPN16() = genericTest("gpn16", false)

    @Test
    fun testGPN15() = genericTest("gpn15", false)

    @Test
    fun testGPN14() = genericTest("gpn14", false)

    @Test
    fun test36c3() = genericTest("36c3", true)

    @Test
    fun chaosradio() = genericTest("chaosradio", false)

    @Test
    fun fiffkon18() = genericTest("fiffkon18", false)

    @Test
    fun denog7() = genericTest("denog7", false)

    @Test
    fun denog8() = genericTest("denog8", false)

    @Test
    fun denog10() = genericTest("denog10", false)

    @Test
    fun denog11() = genericTest("denog11", false)

    @Test
    fun openChaos() = genericTest("openchaos", false)

    fun genericTest(acronym: String, expResult: Boolean) = runBlocking {
        val conference = api.getConferenceByName(acronym).body()
        val map = conference?.events?.map { Event(it) }
        if (map != null) {
            assertTrue(expResult == ChaosflixUtil.areTagsUsefull(map, conference.acronym))
        }
    }
}
