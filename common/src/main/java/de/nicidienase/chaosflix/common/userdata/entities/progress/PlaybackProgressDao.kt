package de.nicidienase.chaosflix.common.userdata.entities.progress

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlaybackProgressDao {
    @Query("SELECT * from playback_progress")
    fun getAll(): LiveData<List<PlaybackProgress>>

    @Query("SELECT * from playback_progress")
    fun getAllSync(): List<PlaybackProgress>

    @Query("SELECT * from playback_progress WHERE event_guid = :guid LIMIT 1")
    fun getProgressForEvent(guid: String): LiveData<PlaybackProgress?>

    @Query("SELECT * from playback_progress WHERE event_guid = :guid LIMIT 1")
    suspend fun getProgressForEventSync(guid: String): PlaybackProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveProgress(progress: PlaybackProgress): Long

    @Query("DELETE from playback_progress WHERE event_guid = :guid")
    fun deleteItem(guid: String)
}