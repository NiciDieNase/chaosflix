package de.nicidienase.chaosflix.common.entities

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import de.nicidienase.chaosflix.common.entities.download.OfflineEvent
import de.nicidienase.chaosflix.common.entities.download.OfflineEventDao
import de.nicidienase.chaosflix.common.entities.recording.persistence.*
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgress
import de.nicidienase.chaosflix.common.entities.userdata.PlaybackProgressDao
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItem
import de.nicidienase.chaosflix.common.entities.userdata.WatchlistItemDao

@Database(entities = arrayOf(
        PersistentConference::class,
        PersistentEvent::class,
        PersistentRecording::class,
        ConferenceGroup::class,
        PlaybackProgress::class,
        WatchlistItem::class,
        OfflineEvent::class), version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ChaosflixDatabase : RoomDatabase() {
    abstract fun playbackProgressDao(): PlaybackProgressDao
    abstract fun watchlistItemDao(): WatchlistItemDao

    abstract fun conferenceDao(): ConferenceDao
    abstract fun eventDao(): EventDao
    abstract fun recordingDao(): RecordingDao

    abstract fun conferenceGroupDao(): ConferenceGroupDao

    abstract fun offlineEventDao(): OfflineEventDao
}
