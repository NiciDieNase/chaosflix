package de.nicidienase.chaosflix.common.entities.download

import android.arch.persistence.room.*
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentEvent
import de.nicidienase.chaosflix.common.entities.recording.persistence.PersistentRecording

@Entity(tableName = "offline_event",
        indices = arrayOf(Index(value = "event_id", unique = true)))
data class OfflineEvent(
        @ColumnInfo(name = "event_id") var eventId: Long,
        @ColumnInfo(name = "recording_id") var recordingId: Long,
        @ColumnInfo(name = "download_reference") var downloadReference: Long,
        @ColumnInfo(name = "local_path") var localPath: String){

    @PrimaryKey(autoGenerate = true) var id: Long = 0

    @Ignore var event: PersistentEvent? = null
    @Ignore var recording: PersistentRecording? = null
}