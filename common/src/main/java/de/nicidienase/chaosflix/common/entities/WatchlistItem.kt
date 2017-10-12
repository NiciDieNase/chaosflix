package de.nicidienase.chaosflix.common.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

import org.joda.time.DateTime

import java.util.Date

/**
 * Created by felix on 19.04.17.
 */

@Entity(tableName = "watchlist_item")
data class WatchlistItem (@PrimaryKey
                          var id: Int,
                          var eventId: Int)
