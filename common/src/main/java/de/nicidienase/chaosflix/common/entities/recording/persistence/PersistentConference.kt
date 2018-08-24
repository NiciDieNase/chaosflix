package de.nicidienase.chaosflix.common.entities.recording.persistence

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import de.nicidienase.chaosflix.common.entities.recording.Conference
import de.nicidienase.chaosflix.common.entities.recording.Event

@Entity(tableName = "conference")
data class PersistentConference(
        @PrimaryKey
        var conferenceId: Long = 0,
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
        var tagsUsefull: Boolean = false
) : Parcelable {

    @Ignore
    constructor(con: Conference) : this(con.conferenceID,0,
            con.acronym, con.aspectRatio, con.title, con.slug, con.webgenLocation,
            con.scheduleUrl, con.logoUrl, con.imagesUrl, con.recordingsUrl, con.url,
            con.updatedAt,con.tagsUsefull)

    @Ignore
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
            parcel.readString(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(conferenceId)
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
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PersistentConference> {
        override fun createFromParcel(parcel: Parcel): PersistentConference {
            return PersistentConference(parcel)
        }

        override fun newArray(size: Int): Array<PersistentConference?> {
            return arrayOfNulls(size)
        }
    }

}