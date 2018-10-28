package de.nicidienase.chaosflix.common

import android.arch.persistence.room.Room
import android.content.Context

class DatabaseFactory private constructor(context: Context) {

	companion object : SingletonHolder<ChaosflixDatabase, Context>({
		Room.databaseBuilder(
				it.applicationContext,
				ChaosflixDatabase::class.java, "mediaccc.de")
				.addMigrations(
						ChaosflixDatabase.migration_2_3,
						ChaosflixDatabase.migration_3_4,
						ChaosflixDatabase.migration_5_6)
				.fallbackToDestructiveMigrationFrom(4)
				.build()
	})
}