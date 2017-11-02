package de.nicidienase.chaosflix.common.entities.userdata

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by felix on 19.04.17.
 */

@Entity(tableName = "watchlist_item")
data class WatchlistItem(@PrimaryKey
                         var id: Long,
                         var eventId: Long)
