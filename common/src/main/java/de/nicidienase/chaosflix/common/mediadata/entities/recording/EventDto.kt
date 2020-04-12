package de.nicidienase.chaosflix.common.mediadata.entities.recording

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EventDto(
    @SerializedName("conference_id")
    var conferenceId: Long = 0,
    var guid: String = "",
    var title: String = "",
    var subtitle: String?,
    var slug: String = "",
    var link: String? = "",
    var description: String?,
    @SerializedName("original_language")
    var originalLanguage: String? = "",
    var persons: Array<String>?,
    var tags: Array<String?>?,
    var date: String? = "",
    @SerializedName("release_date")
    var releaseDate: String? = "",
    @SerializedName("updated_at")
    var updatedAt: String? = "",
    var length: Long = 0,
    @SerializedName("thumb_url")
    var thumbUrl: String? = "",
    @SerializedName("poster_url")
    var posterUrl: String = "",
    @SerializedName("frontend_link")
    var frontendLink: String? = "",
    var url: String = "",
    @SerializedName("conference_url")
    var conferenceUrl: String = "",
    var recordings: List<RecordingDto>?,
    var related: List<RelatedEventDto>?,
    @SerializedName("promoted")
    var isPromoted: Boolean = false,
    @SerializedName("timeline_url")
    var timelineUrl: String,
    @SerializedName("thumbnails_url")
    var thumbnailsUrl: String
) : Comparable<EventDto> {

    var eventID: Long
    @SerializedName("view_count")
    var viewCount: Int = 0

    init {
        val strings = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        try {
            eventID = strings[strings.size - 1].toLong()
        } catch (e: NumberFormatException) {
            eventID = 0
        }
    }

    override fun compareTo(other: EventDto): Int {
        return slug.compareTo(other.slug)
    }
}
