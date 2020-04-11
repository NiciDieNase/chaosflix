package de.nicidienase.chaosflix.common.userdata.entities.watchlist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_item",
        indices = [Index(value = ["event_guid"], unique = true)])
data class WatchlistItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "event_guid")
    var eventGuid: String
)
