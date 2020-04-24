package de.nicidienase.chaosflix.common.userdata.entities.progress

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "playback_progress",
        indices = [Index(value = ["event_guid"], unique = true)])
data class PlaybackProgress(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "event_guid")
    var eventGuid: String,
    var progress: Long,
    @ColumnInfo(name = "watch_date")
    var watchDate: Long
) {
    var date: Date
        get() = Date(watchDate)
        set(value) { watchDate = value.time }
}
