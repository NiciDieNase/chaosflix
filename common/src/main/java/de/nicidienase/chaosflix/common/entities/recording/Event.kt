package de.nicidienase.chaosflix.common.entities.recording

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.annotations.SerializedName

class Event(@SerializedName("conference_id") var conferenceId: Long,
    var guid: String,
    var title: String,
    var subtitle: String,
    var slug: String,
    var link: String,
    var description: String,
    @SerializedName("original_language") var originalLanguage: String,
    var persons: List<String>,
    var tags: List<String>,
    var date: String,
    @SerializedName("release_date") var releaseDate: String,
    @SerializedName("updated_at") var updatedAt: String,
    var length: Int = 0,
    @SerializedName("thumb_url") var thumbUrl: String,
    @SerializedName("poster_url") var posterUrl: String,
    @SerializedName("frontend_link") var frontendLink: String,
    var url: String,
    @SerializedName("conference_url") var conferenceUrl: String,
    var recordings: List<Recording>,
    var metadata: Metadata,
    var isPromoted: Boolean = false
) : Parcelable, Comparable<Event> {

    @SerializedName("view_count") var viewCount: Int = 0
    val apiID: Int


    init {
        val strings = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        apiID = strings[strings.size - 1].toInt()

        val split = conferenceUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        conferenceId = (split[split.size - 1]).toLong()
    }


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
        length = `in`.readInt(),
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
        dest.writeInt(length)
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
