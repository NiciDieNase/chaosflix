package de.nicidienase.chaosflix.common

import de.nicidienase.chaosflix.common.entities.recording.Conference
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.entities.recording.Event
import de.nicidienase.chaosflix.common.entities.recording.persistence.Metadata
import de.nicidienase.chaosflix.common.entities.recording.Recording
import org.junit.Test
import org.mockito.Mockito.*

class ConferencesWrapperTest{


    @Test
    fun test1(){
        val wrapper = ConferencesWrapper(listOf(Conference("42c3", "16:9", "42c3,", "42c3",
                "foo", "", "", "", "", "foo/42", "",
                listOf(Event(1, "", "", "", "", "", "", "",
                        listOf(""), listOf("foo", "bar"), "", "", "", 1, "",
                        "", "", "foo/42", "foo/42", listOf(mock(Recording::class.java)), mock(Metadata::class.java), false)))))
        assert(wrapper.conferenceMap.keys.size == 2)
    }
}