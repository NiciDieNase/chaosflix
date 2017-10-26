package de.nicidienase.chaosflix.common.entities.userdata

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by felix on 06.04.17.
 */

@Entity(tableName = "playback_progress")
class PlaybackProgress (@PrimaryKey
                        @ColumnInfo(name = "event_id")
                        var eventId: Int,
                        var progress: Long)
