package de.nicidienase.chaosflix.common.entities.userdata

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface PlaybackProgressDao {
    @Query("SELECT * from playback_progress")
    fun getAll(): LiveData<List<PlaybackProgress>>

    @Query("SELECT * from playback_progress WHERE event_id = :id LIMIT 1")
    fun getProgressForEvent(id: Long): LiveData<PlaybackProgress>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveProgress(progress: PlaybackProgress): Long
}