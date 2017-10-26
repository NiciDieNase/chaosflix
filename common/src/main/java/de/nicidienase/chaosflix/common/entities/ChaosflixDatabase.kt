package de.nicidienase.chaosflix.common.entities

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgress
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgressDao
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItem
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItemDao

/**
 * Created by felix on 04.10.17.
 */

@Database(entities = arrayOf(PlaybackProgress::class, WatchlistItem::class), version = 1, exportSchema = false)
abstract class ChaosflixDatabase : RoomDatabase() {
    abstract fun playbackProgressDao(): PlaybackProgressDao
    abstract fun watchlistItemDao(): WatchlistItemDao
}
