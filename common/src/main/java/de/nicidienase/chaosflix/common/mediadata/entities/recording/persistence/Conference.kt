package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import de.nicidienase.chaosflix.common.mediadata.entities.recording.ConferenceDto

@Entity(tableName = "conference",
        indices = [Index(value = ["acronym"], unique = true)])
data class Conference(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var conferenceGroupId: Long = 0,
    var acronym: String = "",
    var aspectRatio: String = "",
    var title: String = "",
    var slug: String = "",
    var webgenLocation: String = "",
    var scheduleUrl: String?,
    var logoUrl: String = "",
    var imagesUrl: String = "",
    var recordingsUrl: String = "",
    var url: String = "",
    var updatedAt: String = "",
    var tagsUsefull: Boolean = false,
    var lastReleasedAt: String = ""
) : Parcelable, Comparable<Conference> {

    @Ignore
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readByte() != 0.toByte(),
            parcel.readString() ?: "") {
    }

    @Ignore
    constructor(con: ConferenceDto) : this(
            acronym = con.acronym,
            aspectRatio = con.aspectRatio,
            title = con.title,
            slug = con.slug,
            webgenLocation = con.webgenLocation,
            scheduleUrl = con.scheduleUrl,
            logoUrl = con.logoUrl,
            imagesUrl = con.imagesUrl,
            recordingsUrl = con.recordingsUrl,
            url = con.url,
            updatedAt = con.updatedAt,
            tagsUsefull = con.tagsUsefull,
            lastReleasedAt = con.lastReleaseAt ?: "")

    override fun compareTo(other: Conference) = lastReleasedAt.compareTo(other.lastReleasedAt) * -1

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(conferenceGroupId)
        parcel.writeString(acronym)
        parcel.writeString(aspectRatio)
        parcel.writeString(title)
        parcel.writeString(slug)
        parcel.writeString(webgenLocation)
        parcel.writeString(scheduleUrl)
        parcel.writeString(logoUrl)
        parcel.writeString(imagesUrl)
        parcel.writeString(recordingsUrl)
        parcel.writeString(url)
        parcel.writeString(updatedAt)
        parcel.writeByte(if (tagsUsefull) 1 else 0)
        parcel.writeString(lastReleasedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Conference> {
        override fun createFromParcel(parcel: Parcel): Conference {
            return Conference(parcel)
        }

        override fun newArray(size: Int): Array<Conference?> {
            return arrayOfNulls(size)
        }
    }
}