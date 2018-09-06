package de.nicidienase.chaosflix.common.userdata.entities.download

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentItem
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.PersistentRecording

@Entity(tableName = "offline_event",
        indices = arrayOf(Index(value = ["event_guid"], unique = true)))
data class OfflineEvent(
        @ColumnInfo(name = "event_guid") var eventGuid: String,
        @ColumnInfo(name = "recording_id") var recordingId: Long,
        @ColumnInfo(name = "download_reference") var downloadReference: Long,
        @ColumnInfo(name = "local_path") var localPath: String): PersistentItem(), Parcelable {

    @Ignore var event: PersistentEvent? = null
    @Ignore var recording: PersistentRecording? = null

    @Ignore
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readString()) {
        event = parcel.readParcelable(PersistentEvent::class.java.classLoader)
        recording = parcel.readParcelable(PersistentRecording::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(eventGuid)
        parcel.writeLong(recordingId)
        parcel.writeLong(downloadReference)
        parcel.writeString(localPath)
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