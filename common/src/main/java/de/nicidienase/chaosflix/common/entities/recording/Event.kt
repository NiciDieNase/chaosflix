package de.nicidienase.chaosflix.common.entities.recording

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import java.util.*

@Entity(tableName = "event")
@JsonIgnoreProperties(ignoreUnknown = true)
open class Event(@JsonProperty("conference_id") var conferenceId: Long,
                 val guid: String,
                 val title: String,
                 val subtitle: String?,
                 val slug: String,
                 val link: String,
                 val description: String,
                 @JsonProperty("original_language") val originalLanguage: String,
                 val persons: List<String>,
                 val tags: List<String>,
                 val date: String,
                 @JsonProperty("release_date") val releaseDate: String,
                 @JsonProperty("updated_at") val updatedAt: String,
                 val length: Long = 0,
                 @JsonProperty("thumb_url") val thumbUrl: String,
                 @JsonProperty("poster_url") val posterUrl: String,
                 @JsonProperty("frontend_link") val frontendLink: String,
                 val url: String,
                 @JsonProperty("conference_url") val conferenceUrl: String,
                 val recordings: List<Recording>?,
                 @Embedded val metadata: Metadata,
                 @JsonProperty("promoted") val isPromoted: Boolean = false
) : Parcelable, Comparable<Event> {

    @PrimaryKey
    val apiID: Long
    @JsonProperty("view_count")
    var viewCount: Int = 0

    init {
        val strings = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        apiID = strings[strings.size - 1].toLong()

        val split = conferenceUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        conferenceId = (split[split.size - 1]).toLong()
    }

    fun getExtendedDescription(): String
        = "$description\n\nreleased at: $releaseDate\n\nTags: ${tags.joinToString(", ")}"

    fun getSpeakerString(): String
        = persons.joinToString(", ")

    protected constructor(`in`: Parcel) : this(
            conferenceId = `in`.readLong(),
            guid = `in`.readString(),
            title = `in`.readString(),
            subtitle = `in`.readString(),
            slug = `in`.readString(),
            link = `in`.readString(),
            description = `in`.readString(),
            originalLanguage = `in`.readString(),
            persons = `in`.createStringArrayList(),
            tags = `in`.createStringArrayList(),
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
        dest.writeStringList(persons)
        dest.writeStringList(tags)
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
        recordings!!
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
