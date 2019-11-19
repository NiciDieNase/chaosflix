package de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import de.nicidienase.chaosflix.common.mediadata.entities.recording.RecordingDto

@Entity(tableName = "recording",
        foreignKeys = arrayOf(ForeignKey(
                entity = Event::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = (arrayOf("id")),
                childColumns = arrayOf("eventId"))),
        indices = arrayOf(
                Index("eventId"),
                Index("url", unique = true),
                Index("backendId", unique = true)))
data class Recording(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var eventId: Long = 0,
    var size: Int = 0,
    var length: Int = 0,
    var mimeType: String = "",
    var language: String = "",
    var filename: String = "",
    var state: String = "",
    var folder: String = "",
    var isHighQuality: Boolean = false,
    var width: Int = 0,
    var height: Int = 0,
    var updatedAt: String = "",
    var recordingUrl: String = "",
    var url: String = "",
    var eventUrl: String = "",
    var conferenceUrl: String = "",
    var backendId: Long = 0
) : Parcelable {

    @Ignore
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readLong(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readByte() != 0.toByte(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readLong()) {
    }

    @Ignore
    constructor(rec: RecordingDto, eventId: Long = 0) : this(
            eventId = eventId,
            size = rec.size,
            length = rec.length,
            mimeType = rec.mimeType,
            language = rec.language,
            filename = rec.filename,
            state = rec.state,
            folder = rec.folder,
            isHighQuality = rec.isHighQuality,
            width = rec.width,
            height = rec.height,
            updatedAt = rec.updatedAt,
            recordingUrl = rec.recordingUrl,
            url = rec.url,
            eventUrl = rec.eventUrl,
            conferenceUrl = rec.conferenceUrl,
            backendId = rec.recordingID)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(eventId)
        parcel.writeInt(size)
        parcel.writeInt(length)
        parcel.writeString(mimeType)
        parcel.writeString(language)
        parcel.writeString(filename)
        parcel.writeString(state)
        parcel.writeString(folder)
        parcel.writeByte(if (isHighQuality) 1 else 0)
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeString(updatedAt)
        parcel.writeString(recordingUrl)
        parcel.writeString(url)
        parcel.writeString(eventUrl)
        parcel.writeString(conferenceUrl)
        parcel.writeLong(backendId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Recording> {
        override fun createFromParcel(parcel: Parcel): Recording {
            return Recording(parcel)
        }

        override fun newArray(size: Int): Array<Recording?> {
            return arrayOfNulls(size)
        }
    }
}