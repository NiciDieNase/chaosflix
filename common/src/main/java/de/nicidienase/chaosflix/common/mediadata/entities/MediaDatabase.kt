package de.nicidienase.chaosflix.common.mediadata.entities

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.*
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem

@Database(entities = arrayOf(
		PersistentItem::class,
		PersistentConference::class,
		PersistentEvent::class,
		PersistentRecording::class,
		PersistentRelatedEvent::class,
		ConferenceGroup::class,
		PlaybackProgress::class,
		WatchlistItem::class,
		OfflineEvent::class), version = 5, exportSchema = true)
@TypeConverters(Converters::class)
abstract class MediaDatabase : RoomDatabase() {

	abstract fun conferenceGroupDao(): ConferenceGroupDao
	abstract fun conferenceDao(): ConferenceDao
	abstract fun eventDao(): EventDao
	abstract fun relatedEventDao(): RelatedEventDao
	abstract fun recordingDao(): RecordingDao

	companion object {
		val migration_2_3 = object : Migration(2, 3) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("CREATE TABLE `offline_event` (" +
						"´id´ INTEGER, " +
						"`event_id` TEXT," +
						"`recording_id` TEXT, " +
						"`download_reference` TEXT" +
						"`local_path` TEXT" +
						"PRIMARY KEY (`id`)")
			}
		}

		val migration_3_4 = object : Migration(3, 4) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("CREATE INDEX IF NOT EXISTS index_recording_eventdId ON recording (eventId)")
				database.execSQL("CREATE INDEX IF NOT EXISTS index_event_eventId ON event (eventId)")
				database.execSQL("CREATE INDEX IF NOT EXISTS index_event_frontendLink ON event (frontendLink)")
				database.execSQL("CREATE INDEX IF NOT EXISTS index_event_conferenceId ON event (conferenceId)")
			}
		}

		val migration_4_5 = object : Migration(4,5){
			override fun migrate(database: SupportSQLiteDatabase) {
				TODO("not implemented")
			}

		}
	}
}


