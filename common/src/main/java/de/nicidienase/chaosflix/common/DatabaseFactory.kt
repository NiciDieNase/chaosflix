package de.nicidienase.chaosflix.common

import android.arch.persistence.room.Room
import android.content.Context

class DatabaseFactory (context: Context) {
    val mediaDatabase by lazy {
        Room.databaseBuilder(
                context.applicationContext,
                ChaosflixDatabase::class.java, "mediaccc.de")
                .fallbackToDestructiveMigrationFrom(4)
                .build()
    }
}