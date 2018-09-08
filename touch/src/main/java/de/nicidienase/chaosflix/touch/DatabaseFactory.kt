package de.nicidienase.chaosflix.touch

import android.arch.persistence.room.Room
import android.content.Context
import de.nicidienase.chaosflix.common.ChaosflixDatabase

class DatabaseFactory (context: Context) {
    val database = Room.databaseBuilder(
            context.applicationContext,
            ChaosflixDatabase::class.java, "mediaccc.de")
//            .addMigrations(
//                    ChaosflixDatabase.migration_2_3,
//                    ChaosflixDatabase.migration_3_4)
            .fallbackToDestructiveMigrationFrom(4)
            .build()
}
