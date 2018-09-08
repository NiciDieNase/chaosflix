package de.nicidienase.chaosflix.common

import de.nicidienase.chaosflix.common.entities.recording.Conference
import de.nicidienase.chaosflix.common.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.entities.recording.Event
import org.junit.Ignore
import org.junit.Test

class ConferencesWrapperTest{


    @Test
    @Ignore
    fun test1(){
        val wrapper =
                ConferencesWrapper(
                        listOf(
                                Conference("42c3", "16:9", "", "42c3,",
                                        "42c3","congress/-1c3", "", "",
                                        "", "", "foo/42", "",
                                        listOf(
                                                Event(1, "", "", "", "",
                                                        "", "", "", arrayOf(""),
                                                        arrayOf("foo", "bar"), "", "", "",
                                                        1, "", "", "", "foo/42",
                                                        "foo/42", emptyList(), emptyList(), false
                                                )
                                        )
                                )
                        )
                )
        assert(wrapper.conferencesMap.keys.size == 1)
    }
}