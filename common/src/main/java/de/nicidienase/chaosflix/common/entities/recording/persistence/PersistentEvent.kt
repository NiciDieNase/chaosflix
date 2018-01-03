package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import de.nicidienase.chaosflix.common.entities.recording.Event
import de.nicidienase.chaosflix.common.entities.recording.Metadata
import de.nicidienase.chaosflix.common.entities.recording.Recording
import java.util.*

@Entity(tableName = "event",
        foreignKeys = arrayOf(ForeignKey(
                entity = PersistentConference::class,
                parentColumns = (arrayOf("conferenceId")),
                childColumns = arrayOf("conferenceId"))),
        indices = arrayOf(Index("eventId"), Index("frontendLink"),Index("conferenceId")))

data class PersistentEvent(@PrimaryKey(autoGenerate = false)
                           var eventId: Long = 0,
                           var conferenceId: Long = 0,
                           var guid: String = "",
                           var title: String = "",
                           var subtitle: String? = "",
                           var slug: String = "",
                           var link: String? = "",
                           var description: String? = "",
                           var originalLanguage: String = "",
                           var date: String? = "",
                           var releaseDate: String = "",
                           var updatedAt: String = "",
                           var length: Long = 0,
                           var thumbUrl: String = "",
                           var posterUrl: String = "",
                           var frontendLink: String? = "",
                           var url: String = "",
                           var conferenceUrl: String = "",
                           @Embedded
                           var metadata: Metadata? = null,

                           var isPromoted: Boolean = false,
                           var viewCount: Int = 0,

                           var persons: Array<String>? = null,
                           var tags: Array<String>? = null,
                           @Ignore
                           var recordings: List<Recording>? = null
): Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(Metadata::class.java.classLoader),
            parcel.readByte() != 0.toByte(),
            parcel.readInt(),
            parcel.createStringArray(),
            parcel.createStringArray())

    fun getExtendedDescription(): String
            = "$description\n\nreleased at: $releaseDate\n\nTags: ${tags?.joinToString(", ")}"

    fun getSpeakerString(): String?
            = persons?.joinToString(", ")

    @Ignore
    constructor(event: Event) : this(event.eventID,
            event.conferenceId, event.guid, event.title,
            event.subtitle, event.slug, event.link, event.description,
            event.originalLanguage, event.date, event.releaseDate,
            event.updatedAt, event.length, event.thumbUrl, event.posterUrl,
            event.frontendLink, event.url, event.conferenceUrl,
            event.metadata, event.isPromoted, event.viewCount,
            event.persons, event.tags, event.recordings)

    fun toEvent(): Event = Event(conferenceId, guid, title, subtitle, slug, link, description,
            originalLanguage, persons, tags, date, releaseDate, updatedAt, length,
            thumbUrl, posterUrl, frontendLink, url, conferenceUrl, recordings, metadata, isPromoted)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(eventId)
        parcel.writeLong(conferenceId)
        parcel.writeString(guid)
        parcel.writeString(title)
        parcel.writeString(subtitle)
        parcel.writeString(slug)
        parcel.writeString(link)
        parcel.writeString(description)
        parcel.writeString(originalLanguage)
        parcel.writeString(date)
        parcel.writeString(releaseDate)
        parcel.writeString(updatedAt)
        parcel.writeLong(length)
        parcel.writeString(thumbUrl)
        parcel.writeString(posterUrl)
        parcel.writeString(frontendLink)
        parcel.writeString(url)
        parcel.writeString(conferenceUrl)
        parcel.writeParcelable(metadata, flags)
        parcel.writeByte(if (isPromoted) 1 else 0)
        parcel.writeInt(viewCount)
        parcel.writeStringArray(persons)
        parcel.writeStringArray(tags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PersistentEvent> {
        override fun createFromParcel(parcel: Parcel): PersistentEvent {
            return PersistentEvent(parcel)
        }

        override fun newArray(size: Int): Array<PersistentEvent?> {
            return arrayOfNulls(size)
        }
    }
}