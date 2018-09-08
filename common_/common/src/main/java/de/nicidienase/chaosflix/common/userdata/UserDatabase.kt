package de.nicidienase.chaosflix.common.userdata

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import de.nicidienase.chaosflix.common.userdata.entities.download.*
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgressDao
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItemDao

@Database(entities = arrayOf(
		PlaybackProgress::class,
		WatchlistItem::class,
		OfflineEvent::class), version = 1, exportSchema = true)
abstract class UserDatabase : RoomDatabase() {
	abstract fun playbackProgressDao(): PlaybackProgressDao
	abstract fun watchlistItemDao(): WatchlistItemDao
	abstract fun offlineEventDao(): OfflineEventDao
}


