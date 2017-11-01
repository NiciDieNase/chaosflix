package de.nicidienase.chaosflix.common.entities.recording

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import de.nicidienase.chaosflix.common.entities.recording.persistence.Metadata

import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
open class Event(@JsonProperty("conference_id")
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
) : Parcelable, Comparable<Event> {

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

    protected constructor(`in`: Parcel) : this(
            conferenceId = `in`.readLong(),
            guid = `in`.readString(),
            title = `in`.readString(),
            subtitle = `in`.readString(),
            slug = `in`.readString(),
            link = `in`.readString(),
            description = `in`.readString(),
            originalLanguage = `in`.readString(),
            persons = `in`.createStringArray(),
            tags = `in`.createStringArray(),
            date = `in`.readString(),
            releaseDate = `in`.readString(),
            updatedAt = `in`.readString(),
            length = `in`.readLong(),
            thumbUrl = `in`.readString(),
            posterUrl = `in`.readString(),
            frontendLink = `in`.readString(),
            url = `in`.readString(),
            conferenceUrl = `in`.readString(),
            recordings = `in`.createTypedArrayList(Recording.CREATOR),
            metadata = `in`.readParcelable(Metadata::class.java.classLoader)
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(conferenceId)
        dest.writeString(guid)
        dest.writeString(title)
        dest.writeString(subtitle)
        dest.writeString(slug)
        dest.writeString(link)
        dest.writeString(description)
        dest.writeString(originalLanguage)
        dest.writeStringArray(persons)
        dest.writeStringArray(tags)
        dest.writeString(date)
        dest.writeString(releaseDate)
        dest.writeString(updatedAt)
        dest.writeLong(length)
        dest.writeString(thumbUrl)
        dest.writeString(posterUrl)
        dest.writeString(frontendLink)
        dest.writeString(url)
        dest.writeString(conferenceUrl)
        dest.writeTypedList(recordings)
        dest.writeParcelable(metadata, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

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

    fun getOptimalStream(): Recording {
        val recordings = recordings!!

        val result = ArrayList<Recording>()
        for (r in recordings) {
            if (r.isHighQuality && r.mimeType == "video/mp4")
                result.add(r)
        }
        if (result.size == 0) {
            for (r in recordings) {
                if (r.mimeType == "video/mp4")
                    result.add(r)
            }
        }
        if (result.size == 0) {
            for (r in recordings) {
                if (r.mimeType.startsWith("video/"))
                    result.add(r)
            }
        }
        // sort by length of language-string in decending order, so first item has most languages
        Collections.sort(result) { o1, o2 -> o2.language.length - o1.language.length }
        return if (result.size > 0) {
            result[0]
        } else {
            recordings.get(0)
        }
    }

    companion object {

        val CREATOR: Parcelable.Creator<Event> = object : Parcelable.Creator<Event> {
            override fun createFromParcel(`in`: Parcel): Event {
                return Event(`in`)
            }

            override fun newArray(size: Int): Array<Event?> {
                return arrayOfNulls(size)
            }
        }
    }
}
