package de.nicidienase.chaosflix.common.entities.userdata

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "watchlist_item",
        indices = arrayOf(Index(value = "event_id",unique = true)))
data class WatchlistItem(@PrimaryKey
                         var id: Long,
                         @ColumnInfo(name = "event_id")
                         var eventId: Long)
