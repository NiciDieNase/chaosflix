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
    val id: Long = 0,
    @ColumnInfo(name = "event_guid")
    var eventGuid: String,
    var progress: Long,
    @ColumnInfo(name = "watch_date")
    val watchDate: Long
) {
    val date: Date
    get() = Date(watchDate)
}
