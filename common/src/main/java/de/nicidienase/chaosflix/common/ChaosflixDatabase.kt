package de.nicidienase.chaosflix.common

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import de.nicidienase.chaosflix.common.mediadata.entities.Converters
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceDao
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroup
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.ConferenceGroupDao
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.EventDao
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Conference
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Recording
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.RelatedEvent
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.RecordingDao
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.RelatedEventDao
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.userdata.entities.download.OfflineEventDao
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgress
import de.nicidienase.chaosflix.common.userdata.entities.progress.PlaybackProgressDao
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItem
import de.nicidienase.chaosflix.common.userdata.entities.watchlist.WatchlistItemDao

@Database(entities = arrayOf(
        Conference::class,
        Event::class,
        Recording::class,
        RelatedEvent::class,
        ConferenceGroup::class,

        PlaybackProgress::class,
        WatchlistItem::class,
        OfflineEvent::class
), version = 6, exportSchema = true)
@TypeConverters(Converters::class)
abstract class ChaosflixDatabase : RoomDatabase() {

    abstract fun conferenceGroupDao(): ConferenceGroupDao
    abstract fun conferenceDao(): ConferenceDao
    abstract fun eventDao(): EventDao
    abstract fun relatedEventDao(): RelatedEventDao
    abstract fun recordingDao(): RecordingDao

    abstract fun playbackProgressDao(): PlaybackProgressDao
    abstract fun watchlistItemDao(): WatchlistItemDao
    abstract fun offlineEventDao(): OfflineEventDao

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

        val migration_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE playback_progress RENAME TO old_playback_progress")
                database.execSQL("CREATE TABLE IF NOT EXISTS `playback_progress` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`event_guid` TEXT NOT NULL, " +
                        "`progress` INTEGER NOT NULL, " +
                        "`watch_date` INTEGER NOT NULL)")
                database.execSQL("INSERT INTO `playback_progress` (id, event_guid, progress, watch_date) " +
                        "SELECT id, event_guid, progress, 0 from old_playback_progress")
                database.execSQL("DROP TABLE old_playback_progress")
                database.execSQL("ALTER TABLE conference ADD COLUMN lastReleasedAt TEXT NOT NULL DEFAULT '1970-01-01'")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_recording_backendId ON recording (backendId)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_related_parentEventId_relatedEventGuid ON related (parentEventId, relatedEventGuid)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_playback_progress_event_guid ON playback_progress (event_guid)")
            }
        }
    }
}