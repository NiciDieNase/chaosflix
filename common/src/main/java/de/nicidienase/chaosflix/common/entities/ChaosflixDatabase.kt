package de.nicidienase.chaosflix.common.entities

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * Created by felix on 04.10.17.
 */

@Database(entities = arrayOf(PlaybackProgress::class), version = 5)
abstract class ChaosflixDatabase : RoomDatabase() {
    abstract fun playbackProgressDao(): PlaybackProgressDao
}
