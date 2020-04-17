package de.nicidienase.chaosflix.common.mediadata.entities.streaming

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
class LiveConference(
        var conference: String,
        var slug: String,
        var author: String = "",
        var description: String = "",
        var keywords: String = "",
        var schedule: String = "",
        var startsAt: String = "",
        var endsAt: String = "",
        var isCurrentlyStreaming: Boolean = false,
        var groups: List<Group> = emptyList()
) {
    companion object {

        @JvmStatic
        val dummyObject: LiveConference
            get() {
                return LiveConference(
                        conference = "DummyCon",
                        slug = "duco",
                        author = "Conference McConferenceface",
                        groups = listOf(Group.dummyObject),
                        description = "A placeholder conference"
                )
            }

        fun parseDate(dateString: String): Date? {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SSZ", Locale.getDefault()).parse(dateString)
        }
    }
}
