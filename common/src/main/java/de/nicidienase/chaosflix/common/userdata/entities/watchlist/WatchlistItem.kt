package de.nicidienase.chaosflix.common.userdata.entities.watchlist

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "watchlist_item",
        indices = arrayOf(Index(value = ["event_id"],unique = true)))
data class WatchlistItem(@PrimaryKey(autoGenerate = true)
                         var id: Long = 0,
                         @ColumnInfo(name = "event_id")
                         var eventId: Long)
