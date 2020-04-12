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

    fun getExtendedDescription(): String = "$description\n\nreleased at: $releaseDate\n\nTags: ${tags?.joinToString(", ")}"

    fun getSpeakerString(): String? = persons?.joinToString(", ")

    override fun compareTo(other: EventDto): Int {
        return slug.compareTo(other.slug)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is EventDto) {
            guid == other.guid
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = guid.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (subtitle?.hashCode() ?: 0)
        result = 31 * result + slug.hashCode()
        result = 31 * result + (link?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + originalLanguage.hashCode()
        result = 31 * result + (persons?.contentHashCode() ?: 0)
        result = 31 * result + (tags?.contentHashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + releaseDate.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + length.hashCode()
        result = 31 * result + thumbUrl.hashCode()
        result = 31 * result + posterUrl.hashCode()
        result = 31 * result + (frontendLink?.hashCode() ?: 0)
        result = 31 * result + url.hashCode()
        result = 31 * result + conferenceUrl.hashCode()
        result = 31 * result + (recordings?.hashCode() ?: 0)
        result = 31 * result + (related?.hashCode() ?: 0)
        result = 31 * result + isPromoted.hashCode()
        result = 31 * result + eventID.hashCode()
        result = 31 * result + viewCount
        return result
    }
}
