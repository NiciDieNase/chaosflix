package de.nicidienase.chaosflix.common.userdata.entities.progress

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "playback_progress",
        indices = arrayOf(Index(value = "event_id",unique = true)))
data class PlaybackProgress (@PrimaryKey
                        @ColumnInfo(name = "event_id")
                        var eventId: Long,
                        var progress: Long)
