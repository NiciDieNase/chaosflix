package de.nicidienase.chaosflix.touch

import android.arch.persistence.room.Room
import de.nicidienase.chaosflix.common.entities.ChaosflixDatabase

object DatabaseFactory {
    val database = Room.databaseBuilder(
            ChaosflixApplication.APPLICATION_CONTEXT,
            ChaosflixDatabase::class.java, "mediaccc.de")
            .fallbackToDestructiveMigration()
            .build()
}
