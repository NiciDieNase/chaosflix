package de.nicidienase.chaosflix.common.entities.download

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "offline_event",
        indices = arrayOf(Index(value = "event_id", unique = true)))
data class OfflineEvent(
        @ColumnInfo(name = "event_id") var eventId: Long,
        @ColumnInfo(name = "recording_id") var recordingId: Long,
        @ColumnInfo(name = "download_reference") var downloadReference: Long,
        @ColumnInfo(name = "local_path") var localPath: String){

    @PrimaryKey(autoGenerate = true) var id: Long = 0
}