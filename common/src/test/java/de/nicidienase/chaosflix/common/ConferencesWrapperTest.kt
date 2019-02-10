package de.nicidienase.chaosflix.common

import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferenceDto
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferencesWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.recording.EventDto
import org.junit.Ignore
import org.junit.Test

class ConferencesWrapperTest {


	@Test
	@Ignore
	fun test1() {
		val wrapper =
				ConferencesWrapper(
						listOf(
								ConferenceDto("42c3", "16:9", "", "42c3,",
										"42c3", "congress/-1c3", "", "",
										"", "", "foo/42", "",
										listOf(
												EventDto(1, "", "", "", "",
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