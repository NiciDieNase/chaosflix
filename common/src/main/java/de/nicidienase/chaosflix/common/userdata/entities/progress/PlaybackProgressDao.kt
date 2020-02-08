package de.nicidienase.chaosflix.common.userdata.entities.progress

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface PlaybackProgressDao {
    @Query("SELECT * from playback_progress")
    fun getAll(): LiveData<List<PlaybackProgress>>

    @Query("SELECT * from playback_progress")
    fun getAllSync(): List<PlaybackProgress>

    @Query("SELECT * from playback_progress WHERE event_guid = :guid LIMIT 1")
    fun getProgressForEvent(guid: String): LiveData<PlaybackProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveProgress(progress: PlaybackProgress): Long

    @Query("DELETE from playback_progress WHERE event_guid = :guid")
    fun deleteItem(guid: String)
}
