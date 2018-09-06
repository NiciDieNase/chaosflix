package de.nicidienase.chaosflix.common

import android.arch.persistence.room.Room
import android.content.Context

class DatabaseFactory (context: Context) {
    val mediaDatabase by lazy {
        Room.databaseBuilder(
                context.applicationContext,
                ChaosflixDatabase::class.java, "mediaccc.de")
                .addMigrations(
                        ChaosflixDatabase.migration_2_3,
                        ChaosflixDatabase.migration_3_4)
                .fallbackToDestructiveMigration()
                .build()
    }
}
