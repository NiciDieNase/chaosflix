package de.nicidienase.chaosflix.common.entities.recording

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Event(@JsonProperty("conference_id")
                 var conferenceId: Long = 0,
                 var guid: String = "",
                 var title: String = "",
                 var subtitle: String? = "",
                 var slug: String = "",
                 var link: String? = "",
                 var description: String? = "",
                 @JsonProperty("original_language")
                 var originalLanguage: String = "",
                 var persons: Array<String>?,
                 var tags: Array<String>?,
                 var date: String? = "",
                 @JsonProperty("release_date")
                 var releaseDate: String = "",
                 @JsonProperty("updated_at")
                 var updatedAt: String = "",
                 var length: Long = 0,
                 @JsonProperty("thumb_url")
                 var thumbUrl: String = "",
                 @JsonProperty("poster_url")
                 var posterUrl: String = "",
                 @JsonProperty("frontend_link")
                 var frontendLink: String? = "",
                 var url: String = "",
                 @JsonProperty("conference_url")
                 var conferenceUrl: String = "",
                 var recordings: List<Recording>?,
                 var metadata: Metadata?,
                 @JsonProperty("promoted")
                 var isPromoted: Boolean = false
) : Comparable<Event> {

    var eventID: Long
    @JsonProperty("view_count")
    var viewCount: Int = 0

    init {
        val strings = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        eventID = strings[strings.size - 1].toLong()

        val split = conferenceUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        conferenceId = (split[split.size - 1]).toLong()
    }

    fun getExtendedDescription(): String
        = "$description\n\nreleased at: $releaseDate\n\nTags: ${tags?.joinToString(", ")}"

    fun getSpeakerString(): String?
        = persons?.joinToString(", ")

    override fun compareTo(event: Event): Int {
        return slug.compareTo(event.slug)
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is Event) {
            guid == obj.guid
        } else {
            super.equals(obj)
        }

    }
}
