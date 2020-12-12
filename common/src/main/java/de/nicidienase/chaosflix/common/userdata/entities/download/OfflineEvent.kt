package de.nicidienase.chaosflix.common.userdata.entities.download

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording

@Entity(tableName = "offline_event",
        indices = [Index(value = ["event_guid"], unique = true)])
data class OfflineEvent(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "event_guid") var eventGuid: String,
    @ColumnInfo(name = "recording_id") var recordingId: Long,
    @ColumnInfo(name = "download_reference") var downloadReference: Long,
    @ColumnInfo(name = "local_path") var localPath: String,
    var status: Int,
    @ColumnInfo(name = "status_icon") var statusIcon: Int,
    @ColumnInfo(name = "current_bytes") var currentBytes: Int,
    @ColumnInfo(name = "total_bytes") var totalBytes: Int
) : Parcelable {

    @Ignore var event: Event? = null
    @Ignore var recording: Recording? = null

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readLong(),
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()) {
        event = parcel.readParcelable(Event::class.java.classLoader)
        recording = parcel.readParcelable(Recording::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(eventGuid)
        parcel.writeLong(recordingId)
        parcel.writeLong(downloadReference)
        parcel.writeString(localPath)
        parcel.writeInt(status)
        parcel.writeInt(statusIcon)
        parcel.writeInt(currentBytes)
        parcel.writeInt(totalBytes)
        parcel.writeParcelable(event, flags)
        parcel.writeParcelable(recording, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OfflineEvent> {
        override fun createFromParcel(parcel: Parcel): OfflineEvent {
            return OfflineEvent(parcel)
        }

        override fun newArray(size: Int): Array<OfflineEvent?> {
            return arrayOfNulls(size)
        }
    }
}
